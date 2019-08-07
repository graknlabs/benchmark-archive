/*
 *  GRAKN.AI - THE KNOWLEDGE GRAPH
 *  Copyright (C) 2019 Grakn Labs Ltd
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

package grakn.benchmark.querygen;

import grakn.client.GraknClient;
import grakn.core.concept.type.RelationType;
import grakn.core.concept.type.Role;
import grakn.core.concept.type.Type;
import grakn.core.rule.GraknTestServer;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlQuery;
import graql.lang.statement.Variable;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class QueryGeneratorIT {

    private static final String testKeyspace = "querygen_test";

    @ClassRule
    public static final GraknTestServer server = new GraknTestServer(
            Paths.get("querygen/test-integration/conf/grakn.properties"),
            Paths.get("querygen/test-integration/conf/cassandra-embedded.yaml")
    );

    @BeforeClass
    public static void loadSchema() {
        Path path = Paths.get("querygen");
        GraknClient client = new GraknClient(server.grpcUri());
        GraknClient.Session session = client.session(testKeyspace);
        GraknClient.Transaction transaction = session.transaction().write();

        try {
            List<String> lines = Files.readAllLines(Paths.get("querygen/test-integration/resources/schema.gql"));
            String graqlQuery = String.join("\n", lines);
            transaction.execute((GraqlQuery) Graql.parse(graqlQuery));
            transaction.commit();
        } catch (IOException e) {
            e.printStackTrace();
        }

        session.close();
        client.close();
    }

    @Test
    public void queryGeneratorReturnsCorrectNumberOfQueries() {
        try (GraknClient client = new GraknClient(server.grpcUri());
             GraknClient.Session session = client.session(testKeyspace)) {
            QueryGenerator queryGenerator = new QueryGenerator(session);
            int queriesToGenerate = 100;
            List<GraqlGet> queries = queryGenerator.generate(queriesToGenerate);
            assertEquals(queries.size(), queriesToGenerate);
            for (GraqlGet query : queries) {
                assertNotNull(query);
                System.out.println(query);
            }
        }

    }


    /**
     * Test that a single new query is generated as a QueryBuilder
     * This query builder should have all the reserved vars mapped to a type
     */
    @Test
    public void newQueryIsReturnedAsBuilderWithAllVarsMapped() {
        // a empty queryGenerator

        try (GraknClient client = new GraknClient(server.grpcUri());
             GraknClient.Session session = client.session(testKeyspace)) {

            QueryGenerator queryGenerator = new QueryGenerator(session);
            try (GraknClient.Transaction tx = session.transaction().write()){
                // directly generate a new query which contains concepts bound to this tx
                QueryBuilder queryBuilder = queryGenerator.generateNewQuery(tx);

                int generatedVars = queryBuilder.nextVar;
                assertEquals(queryBuilder.variableTypeMap.size(), generatedVars);
            }
        }
    }


    /**
     * QueryBuilder contains mappings from owner types
     */
    @Test
    public void ownedVariablesAreMappedToAttributeTypes() {

        try (GraknClient client = new GraknClient(server.grpcUri());
             GraknClient.Session session = client.session(testKeyspace)) {

            QueryGenerator queryGenerator = new QueryGenerator(session);
            try (GraknClient.Transaction tx = session.transaction().write()) {

                // directly generate a new query which contains concepts bound to this tx
                QueryBuilder queryBuilder = queryGenerator.generateNewQuery(tx);

                for (Variable attributeOwned : queryBuilder.attributeOwnership.values().stream().flatMap(Collection::stream).collect(Collectors.toSet())) {
                    Type attributeOwnedType = queryBuilder.getType(attributeOwned);
                    assertTrue(attributeOwnedType.isAttributeType());
                }

            }
        }
    }

    /**
     * We only want to generate queries that are connected
     */
    @Test
    public void queryVariablesAreConnected() {

        try (GraknClient client = new GraknClient(server.grpcUri());
             GraknClient.Session session = client.session(testKeyspace)) {

            QueryGenerator queryGenerator = new QueryGenerator(session);
            try (GraknClient.Transaction tx = session.transaction().write()) {
                for (int i = 0; i < 20; i++) {
                    QueryBuilder queryBuilder = queryGenerator.generateNewQuery(tx);

                    Set<Variable> allVariables = new HashSet<>(queryBuilder.variableTypeMap.keySet());
                    Set<Variable> connectedVariables = new HashSet<>(Collections.singleton(allVariables.iterator().next()));

                    boolean changed = true;
                    while (changed) {

                        Set<Variable> newConnectedVariables = new HashSet<>(connectedVariables);
                        for (Map.Entry<Variable, List<Variable>> entry : queryBuilder.attributeOwnership.entrySet()) {
                            for (Variable v : connectedVariables) {
                                if (entry.getKey().equals(v)) {
                                    newConnectedVariables.addAll(entry.getValue());
                                } else if (entry.getValue().contains(v)) {
                                    newConnectedVariables.add(entry.getKey());
                                }
                            }
                        }

                        for (Map.Entry<Variable, List<Pair<Variable, Role>>> rp : queryBuilder.relationRolePlayers.entrySet()) {
                            Set<Variable> rolePlayerVars = rp.getValue().stream().map(Pair::getFirst).collect(Collectors.toSet());
                            for (Variable v : connectedVariables) {
                                if (rp.getKey().equals(v)) {
                                    newConnectedVariables.addAll(rolePlayerVars);
                                } else if (rolePlayerVars.contains(v)) {
                                    newConnectedVariables.add(rp.getKey());
                                }
                            }
                        }

                        changed = newConnectedVariables.size() != connectedVariables.size();
                        connectedVariables = newConnectedVariables;
                    }

                    allVariables.removeAll(connectedVariables);
                    assertTrue(allVariables.isEmpty());
                }

            }
        }
    }

    /**
     * Confirm that the when specifying any roles picked for a relation's role players are actually possible in the schema
     * In other words, the intersection of the relations allowing each of these role players is not empty
     */
    @Test
    public void relationRolesCanOccurTogetherInSchema() {

        try (GraknClient client = new GraknClient(server.grpcUri());
             GraknClient.Session session = client.session(testKeyspace)) {

            QueryGenerator queryGenerator = new QueryGenerator(session);
            try (GraknClient.Transaction tx = session.transaction().write()) {
                for (int i = 0; i < 20; i++) {
                    // directly generate a new query which contains concepts bound to this tx
                    QueryBuilder queryBuilder = queryGenerator.generateNewQuery(tx);

                    for (Map.Entry<Variable, Type> entry : queryBuilder.variableTypeMap.entrySet()) {
                        if (entry.getValue().isRelationType() && queryBuilder.relationRolePlayers.containsKey(entry.getKey())) {
                            List<Pair<Variable, Role>> rolePlayers = queryBuilder.relationRolePlayers.get(entry.getKey());

                            Set<Role> roles = rolePlayers.stream().map(Pair::getSecond).collect(Collectors.toSet());

                            Set<RelationType> relationTypesIntersection = roles.stream()
                                    .map(role -> role.relations().collect(Collectors.toSet()))
                                    .reduce((a, b) -> {
                                        Set<RelationType> copy = new HashSet<>(a);
                                        copy.retainAll(b);
                                        return copy;
                                    })
                                    .get();

                            assertTrue(relationTypesIntersection.size() > 0);
                        }
                    }
                }
            }

        }
    }

}
