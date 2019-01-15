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

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;

/**
 *
 */
public class DataGenerator {

    Grakn.Session session;
    String executionName;
    List<String> schemaDefinition;


    private int iteration = 0;
    private Random rand;

    private boolean initialized = false;
    private ConceptStore storage;

    private SchemaSpecificDataGenerator dataStrategies;

    public DataGenerator(Grakn.Session session, String executionName, List<String> schemaDefinition, int randomSeed) {
        this.session = session;
        this.executionName = executionName;
        this.rand = new Random(randomSeed);
        this.iteration = 0;
        this.schemaDefinition = schemaDefinition;
    }

    public void loadSchema() {
        System.out.println("Initialising keyspace `" + this.session.keyspace() + "`...");
        SchemaManager.initialiseKeyspace(this.session, this.schemaDefinition);
        System.out.println("done");
    }

    public void initializeGeneration() {
        try (Grakn.Transaction tx = session.transaction(GraknTxType.READ)) {
            HashSet<EntityType> entityTypes = SchemaManager.getTypesOfMetaType(tx, "entity");
            HashSet<RelationshipType> relationshipTypes = SchemaManager.getTypesOfMetaType(tx, "relationship");
            HashSet<AttributeType> attributeTypes = SchemaManager.getTypesOfMetaType(tx, "attribute");
            this.storage = new IgniteConceptIdStore(entityTypes, relationshipTypes, attributeTypes);
        }

        this.dataStrategies = SchemaSpecificDataGeneratorFactory.getSpecificStrategy(this.executionName, this.rand, this.storage);
        this.initialized = true;
    }

    public void generate(int graphScaleLimit) {
        if (!this.initialized) {
            throw new GeneratorUninitializedException("generate() can only be called after initializing the generation strategies");
        }

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
                    insertions.forEach(insert -> {
                        HashSet<Concept> insertedConcepts = InsertionAnalysis.getInsertedConcepts(q, insertions);
                        if (insertedConcepts.isEmpty()) {
                            throw new RuntimeException("No concepts were inserted");
                        }
                        insertedConcepts.forEach(concept -> this.storage.addConcept(concept));

                        Set<ConceptId> rolePlayers = InsertionAnalysis.getRolePlayers(q);
                        rolePlayers.forEach(conceptId -> this.storage.addRolePlayer(conceptId.toString()));
                    });
                });
    }

}
