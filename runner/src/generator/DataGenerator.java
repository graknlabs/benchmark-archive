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
import grakn.benchmark.runner.util.SchemaManager;
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
    private final String graphName ;
    private final List<String> schemaDefinition;
    private int iteration;
    private Random rand;

    private ConceptStore storage;

    private SchemaSpecificDataGenerator dataStrategies;

    public DataGenerator(Grakn.Session session, BenchmarkConfiguration config, int randomSeed) {
        this.session = session;
        this.graphName = config.graphName();
        this.rand = new Random(randomSeed);
        this.iteration = 0;
        this.schemaDefinition = config.getGraqlSchema();
        initializeGeneration();
   }

    private void initializeGeneration() {
        // load schema
        LOG.info("Initialising keyspace `" + this.session.keyspace() + "`...");
        SchemaManager.initialiseKeyspace(this.session, this.schemaDefinition);
        // Read schema concepts and create ignite tables
        try (Grakn.Transaction tx = session.transaction(GraknTxType.READ)) {
            HashSet<EntityType> entityTypes = SchemaManager.getTypesOfMetaType(tx, "entity");
            HashSet<RelationshipType> relationshipTypes = SchemaManager.getTypesOfMetaType(tx, "relationship");
            HashSet<AttributeType> attributeTypes = SchemaManager.getTypesOfMetaType(tx, "attribute");
            LOG.info("Initialising ignite...");
            this.storage = new IgniteConceptIdStore(entityTypes, relationshipTypes, attributeTypes);
        }
        this.dataStrategies = SchemaSpecificDataGeneratorFactory.getSpecificStrategy(this.graphName, this.rand, this.storage);
    }

    public void generate(int graphScaleLimit) {

        RouletteWheel<RouletteWheel<TypeStrategyInterface>> operationStrategies = this.dataStrategies.getStrategy();
        /*
        This method can be called multiple times, with a higher numConceptsLimit each time, so that the generation can be
        effectively paused while benchmarking takes place
        */

        GeneratorFactory gf = new GeneratorFactory();
        int graphScale = dataStrategies.getGraphScale();

        while (graphScale < graphScaleLimit) {
            try (Grakn.Transaction tx = session.transaction(GraknTxType.WRITE)) {
                TypeStrategyInterface typeStrategy = operationStrategies.next().next();
                GeneratorInterface generator = gf.create(typeStrategy, tx); // TODO Can we do without creating a new generator each iteration

                // create the stream of insert/match-insert queries
                Stream<Query> queryStream = generator.generate();

                // execute & parse the results
                this.processQueryStream(queryStream);
                iteration++;

                graphScale = dataStrategies.getGraphScale();
                printProgress(graphScale, typeStrategy.getTypeLabel());
                tx.commit();
            }
        }
        System.out.print("\n");
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


    private void printProgress(int graphScale, String generatedTypeLabel) {
        int totalRolePlayers = this.storage.totalRolePlayers();
        int explicitRolePlayers = this.storage.totalExplicitRolePlayers();
        // this should actually == number of implicit relationships!
        int attributeOwners = (totalRolePlayers - explicitRolePlayers)/2;

        int entities = this.storage.totalEntities();
        int explicitRelationships = this.storage.totalExplicitRelationships();
        int attributes = this.storage.totalAttributes();


        int orphanEntities = this.storage.totalOrphanEntities();
        int orphanAttrs = this.storage.totalOrphanAttributes();
        int relDoubleCounts = this.storage.totalRelationshipsRolePlayersOverlap();


        // first order statistics
        double meanInDegree = ((float) explicitRolePlayers)/graphScale;
        double meanRolePlayersPerRelationship = ((float) explicitRolePlayers) / explicitRelationships;
        double meanAttributeOwners = ((float) attributeOwners) / attributes;
        double proportionEntities = ((float) entities) / graphScale;
        double proportionRelationships = ((float) explicitRelationships) / graphScale;
        double proportionAttributes = ((float) attributes) / graphScale;

        // our own density measure
        // compute how many connections (ie role players) there would be if everyone were fully connected to everything
        double maxPossibleConnections = attributes * graphScale + explicitRelationships * graphScale;
        double density = ((float) totalRolePlayers) / maxPossibleConnections;


        // print info to console on one self-erasing line
        System.out.print("\r");
        System.out.print(String.format("[%d] %s Scale: %d\t(%f Deg_Cin, %f Deg_Rout, %f Deg_Aout)\t(%d, %d, %d) Entity/Rel/Attr \t (%d EO, %d AO) \t %f density",
                this.iteration, this.graphName, graphScale, meanInDegree, meanRolePlayersPerRelationship, meanAttributeOwners,
                entities, explicitRelationships, attributes,
                orphanEntities, orphanAttrs, density));

        // write to log verbosely in DEBUG that it doesn't overwrite
        LOG.debug(String.format("----- Iteration %d [%s] ----- ", this.iteration, this.graphName));
        LOG.debug(String.format(">> Generating instances of concept type \"%s\"", generatedTypeLabel));
        LOG.debug(String.format(">> %d - Scale", graphScale));
        LOG.debug(String.format(">> %d, %d, %d - entity, explicit relationships, attributes", entities, explicitRelationships, attributes));
        LOG.debug(String.format(">> %d, %d - entity orphans, attribute orphans ", orphanEntities, orphanAttrs));
        LOG.debug(String.format(">> %d - Total relationship double counts", relDoubleCounts));
        LOG.debug(String.format(">> %f, %f, %f - mean Deg_Cin, mean Deg_Rout, mean Deg_Aout",
                meanInDegree, meanRolePlayersPerRelationship, meanAttributeOwners));
        LOG.debug(String.format(">> %f, %f %f - proportion entities, relationships, attributes",
                proportionEntities, proportionRelationships, proportionAttributes));
        LOG.debug(String.format(">> %f - custom density", density));
    }

}
