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
import grakn.client.answer.Answer;
import grakn.core.rule.GraknTestServer;
import graql.lang.Graql;
import graql.lang.query.GraqlQuery;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class QuerySamplerIT {

    private static final String testKeyspace = "querygen_test";

    @ClassRule
    public static final GraknTestServer server = new GraknTestServer(
            Paths.get("querygen/test-integration/conf/grakn.properties"),
            Paths.get("querygen/test-integration/conf/cassandra-embedded.yaml")
    );

    @BeforeClass
    public static void loadSchema() {
        GraknClient client = new GraknClient(server.grpcUri());
        GraknClient.Session session = client.session(testKeyspace);
        GraknClient.Transaction transaction = session.transaction().write();

        try {
            List<String> lines = Files.readAllLines(Paths.get("querygen/test-integration/resources/schema.gql"));
            String graqlQuery = String.join("\n", lines);
            Set<? extends Answer> answers = transaction.stream((GraqlQuery) Graql.parse(graqlQuery), false).collect(Collectors.toSet());
            transaction.commit();
        } catch (IOException e) {
            e.printStackTrace();
        }

        session.close();
        client.close();
    }


    @Test
    public void queryKMeansSamplerReturnsLimitedNumberOfQueries() {
        try (GraknClient client = new GraknClient(server.grpcUri());
             GraknClient.Session session = client.session(testKeyspace)) {

            int queriesToSample = 30;
            int queriesToGenerate = 300;
            List<VectorisedQuery> queries = QuerySampler.querySampleKMeans(session, queriesToGenerate, queriesToSample, 2);
            assertTrue(queriesToSample >= queries.size());
            for (VectorisedQuery query : queries) {
                assertNotNull(query);
                assertNotNull(query.graqlQuery);
            }
        }
    }

    @Test
    public void queryGriddedSamplerReturnsExactNumberOfQueries() {
        try (GraknClient client = new GraknClient(server.grpcUri());
             GraknClient.Session session = client.session(testKeyspace)) {

            int queriesToSample = 30;
            int queriesToGenerate = 300;
            List<VectorisedQuery> queries = QuerySampler.querySampleGridded(session, queriesToGenerate, queriesToSample, 5);
            assertTrue(queries.size() == queriesToSample);
            for (VectorisedQuery query : queries) {
                assertNotNull(query);
                assertNotNull(query.graqlQuery);
            }
        }
    }
}