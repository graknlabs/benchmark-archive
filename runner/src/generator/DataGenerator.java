/*
 *  GRAKN.AI - THE KNOWLEDGE GRAPH
 *  Copyright (C) 2018 Grakn Labs Ltd
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package grakn.benchmark.runner.generator;

import grakn.benchmark.runner.schemaspecific.SchemaSpecificDataGenerator;
import grakn.benchmark.runner.schemaspecific.SchemaSpecificDataGeneratorFactory;
import grakn.benchmark.runner.util.BenchmarkConfiguration;
import grakn.core.GraknTxType;
import grakn.core.client.Grakn;
import grakn.core.concept.*;
import grakn.core.graql.InsertQuery;
import grakn.core.graql.Query;
import grakn.core.graql.answer.ConceptMap;
import grakn.benchmark.runner.storage.ConceptStore;
import grakn.benchmark.runner.storage.IgniteConceptIdStore;
import grakn.benchmark.runner.storage.InsertionAnalysis;
import grakn.benchmark.runner.storage.SchemaManager;
import grakn.benchmark.runner.strategy.RouletteWheel;
import grakn.benchmark.runner.strategy.TypeStrategyInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Stream;

/**
 *
 */
public class DataGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(DataGenerator.class);

    private final Grakn.Session session;
    private final String executionName;
    private final List<String> schemaDefinition;
    private int iteration;
    private Random rand;

    private ConceptStore storage;

    private SchemaSpecificDataGenerator dataStrategies;

    public DataGenerator(Grakn.Session session, BenchmarkConfiguration config, int randomSeed) {
        this.session = session;
        this.executionName = config.getConfigName();
        this.rand = new Random(randomSeed);
        this.iteration = 0;
        this.schemaDefinition = config.getGraqlSchema();
        initializeGeneration();
    }

    private void initializeGeneration() {
        // load schema
        LOG.debug("Initialising keyspace `" + this.session.keyspace() + "`...");
        SchemaManager.initialiseKeyspace(this.session, this.schemaDefinition);
        // Read schema concepts and create ignite tables
        try (Grakn.Transaction tx = session.transaction(GraknTxType.READ)) {
            HashSet<EntityType> entityTypes = SchemaManager.getTypesOfMetaType(tx, "entity");
            HashSet<RelationshipType> relationshipTypes = SchemaManager.getTypesOfMetaType(tx, "relationship");
            HashSet<AttributeType> attributeTypes = SchemaManager.getTypesOfMetaType(tx, "attribute");

            LOG.debug("Initialising ignite...");
            this.storage = new IgniteConceptIdStore(entityTypes, relationshipTypes, attributeTypes);
        }
        this.dataStrategies = SchemaSpecificDataGeneratorFactory.getSpecificStrategy(this.executionName, this.rand, this.storage);
    }

    public void generate(int graphScaleLimit) {

        RouletteWheel<RouletteWheel<TypeStrategyInterface>> operationStrategies = this.dataStrategies.getStrategy();
        /*
        This method can be called multiple times, with a higher numConceptsLimit each time, so that the generation can be
        effectively paused while benchmarking takes place
        */

        GeneratorFactory gf = new GeneratorFactory();
        int graphScale= dataStrategies.getGraphScale();

        while (graphScale < graphScaleLimit) {
            System.out.printf("\n---- Iteration %d ----\n", this.iteration);
            try (Grakn.Transaction tx = session.transaction(GraknTxType.WRITE)) {

                //TODO Deal with this being an Object. TypeStrategy should be/have an interface for this purpose?
                TypeStrategyInterface typeStrategy = operationStrategies.next().next();
                System.out.print("Generating instances of concept type \"" + typeStrategy.getTypeLabel() + "\"\n");

                GeneratorInterface generator = gf.create(typeStrategy, tx); // TODO Can we do without creating a new generator each iteration

                System.out.println("Using generator " + generator.getClass().toString());
                // create the stream of insert/match-insert queries
                Stream<Query> queryStream = generator.generate();

                // execute & parse the results
                this.processQueryStream(queryStream);

                iteration++;
                graphScale = dataStrategies.getGraphScale();
                System.out.printf(String.format("Size: %d (based on ignite data)\n", graphScale));
                System.out.println(String.format("   %d role players", this.storage.totalRolePlayers()));
                System.out.println(String.format("   %d entity orphans", this.storage.totalOrphanEntities()));
                System.out.println(String.format("   %d attribute orphans", this.storage.totalOrphanAttributes()));
                System.out.println(String.format("   %d Rel double counts", this.storage.totalRelationshipsRolePlayersOverlap()));
                System.out.println(String.format("   %d Relationships", this.storage.totalRelationships()));

                tx.commit();
            }
        }
    }

    private void processQueryStream(Stream<Query> queryStream) {
        /*
        Make the data insertions from the stream of queries generated
         */
        queryStream.map(q -> (InsertQuery) q)
                .forEach(q -> {
                    List<ConceptMap> insertions = q.execute();
                    HashSet<Concept> insertedConcepts = InsertionAnalysis.getInsertedConcepts(q, insertions);
                    if (insertedConcepts.isEmpty()) {
                        throw new RuntimeException("No concepts were inserted");
                    }
                    insertedConcepts.forEach(concept -> this.storage.addConcept(concept));

                    // check if we have to update any roles by first checking if any relationships added
                    String relationshipAdded = InsertionAnalysis.getRelationshipTypeLabel(q);
                    if (relationshipAdded != null) {
                        Map<Concept, String> rolePlayersAdded = InsertionAnalysis.getRolePlayersAndRoles(q, insertions);

                        rolePlayersAdded.entrySet().stream()
                                .forEach(entry ->
                                        this.storage.addRolePlayer(
                                                entry.getKey().id().toString(),
                                                entry.getKey().asThing().type().label().toString(),
                                                relationshipAdded,
                                                entry.getValue()
                                        ));
                    }
                });
    }

}
