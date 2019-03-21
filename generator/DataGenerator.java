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

package grakn.benchmark.generator;

import grakn.benchmark.generator.query.QueryProvider;
import grakn.benchmark.generator.storage.ConceptStorage;
import grakn.benchmark.profiler.analysis.InsertQueryAnalyser;
import grakn.core.client.GraknClient;
import grakn.core.concept.Concept;
import grakn.core.concept.answer.ConceptMap;
import graql.lang.query.GraqlInsert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Entry point for Generator.
 * This class is in charge of populating a keyspace executing insert queries provided by schema
 * specific data generators.
 * While populating a keyspace it also updates local storage to keep track of what's already
 * in the current graph.
 */
public class DataGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(DataGenerator.class);

    private final GraknClient client;
    private final String keyspace;
    private final String dataGenerator;
    private final QueryProvider queryProvider;
    private final ConceptStorage storage;

    private int iteration;


    public DataGenerator(GraknClient client, String keyspace, ConceptStorage storage, String dataGenerator, QueryProvider queryProvider) {
        this.client = client;
        this.keyspace = keyspace;
        this.dataGenerator = dataGenerator;
        this.queryProvider = queryProvider;
        this.iteration = 0;
        this.storage = storage;
    }

    /**
     * This method can be called multiple times, with a higher numConceptsLimit each time, so that the generation can be
     * effectively paused while benchmarking takes place
     *
     * @param graphScaleLimit
     */
    public void generate(int graphScaleLimit) {

        GraknClient.Session session = client.session(keyspace);

        while (storage.getGraphScale() < graphScaleLimit) {
            try (GraknClient.Transaction tx = session.transaction().write()) {

                // create the stream of insert/match-insert queries
                Iterator<GraqlInsert> queryStream = queryProvider.nextQueryBatch();

                // execute & parse the results
                processQueryStream(queryStream, tx);

                printProgress();
                tx.commit();
            }
            iteration++;
        }

        session.close();
        System.out.print("\n");
    }

    private void processQueryStream(Iterator<GraqlInsert> queryIterator, GraknClient.Transaction tx) {
        /*
        Make the data insertions from the stream of queries generated
         */
        queryIterator.forEachRemaining(q -> {

            List<ConceptMap> insertions = tx.execute(q);
            HashSet<Concept> insertedConcepts = InsertQueryAnalyser.getInsertedConcepts(q, insertions);

            insertedConcepts.forEach(storage::addConcept);

            // check if we have to update any roles by first checking if any relationships added
            String relationshipAdded = InsertQueryAnalyser.getRelationshipTypeLabel(q);
            if (relationshipAdded != null) {
                Map<String, List<Concept>> rolePlayersAdded = InsertQueryAnalyser.getRolePlayersAndRoles(q, insertions);

                rolePlayersAdded.forEach((roleName, conceptList) -> {
                    conceptList.forEach(concept -> {
                        String rolePlayerId = concept.id().toString();
                        String rolePlayerTypeLabel = concept.asThing().type().label().toString();
                        storage.addRolePlayer(rolePlayerId, rolePlayerTypeLabel, relationshipAdded, roleName);
                    });
                });
            }
        });
    }


    private void printProgress() {
        int graphScale = storage.getGraphScale();
        int totalRolePlayers = this.storage.totalRolePlayers();
        int explicitRolePlayers = this.storage.totalExplicitRolePlayers();
        // this should actually == number of implicit relationships!
        int attributeOwners = (totalRolePlayers - explicitRolePlayers) / 2;

        int entities = this.storage.totalEntities();
        int explicitRelationships = this.storage.totalExplicitRelationships();
        int attributes = this.storage.totalAttributes();

        int implicitRelationships = this.storage.totalImplicitRelationships();


        int orphanEntities = this.storage.totalOrphanEntities();
        int orphanAttrs = this.storage.totalOrphanAttributes();
        int relDoubleCounts = this.storage.totalRelationshipsRolePlayersOverlap();


        // first order statistics
        double meanInDegree = ((float) explicitRolePlayers) / graphScale;
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
        System.out.print(String.format("[%d] %s Scale: %d\t(%f Deg_Cin, %f Deg_Rout, %f Deg_Aout)\t(%d, %d, %d, %d) Entity/Expl Rel/Impl Rel/Attr \t (%d EO, %d AO) \t %f density",
                this.iteration, this.dataGenerator, graphScale, meanInDegree, meanRolePlayersPerRelationship, meanAttributeOwners,
                entities, explicitRelationships, implicitRelationships, attributes,
                orphanEntities, orphanAttrs, density));

        // write to log verbosely in DEBUG that it doesn't overwrite
        LOG.debug(String.format("----- Iteration %d [%s] ----- ", this.iteration, this.dataGenerator));
//        LOG.debug(String.format(">> Generating instances of concept type \"%s\"", generatedTypeLabel));
        LOG.debug(String.format(">> %d - Scale", graphScale));
        LOG.debug(String.format(">> %d, %d, %d, %d - entity, explicit rels, implicit rels, attributes", entities, explicitRelationships, implicitRelationships, attributes));
        LOG.debug(String.format(">> %d, %d - entity orphans, attribute orphans ", orphanEntities, orphanAttrs));
        LOG.debug(String.format(">> %d - Total relationship double counts", relDoubleCounts));
        LOG.debug(String.format(">> %f, %f, %f - mean Deg_Cin, mean Deg_Rout, mean Deg_Aout",
                meanInDegree, meanRolePlayersPerRelationship, meanAttributeOwners));
        LOG.debug(String.format(">> %f, %f %f - proportion entities, relationships, attributes",
                proportionEntities, proportionRelationships, proportionAttributes));
        LOG.debug(String.format(">> %f - custom density", density));
    }

}
