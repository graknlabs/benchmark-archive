package grakn.benchmark.querygen;

import grakn.client.GraknClient;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
            transaction.execute((GraqlQuery) Graql.parse(graqlQuery));
            transaction.commit();
        } catch (IOException e) {
            e.printStackTrace();
        }

        session.close();
        client.close();
        System.out.println("Loaded schema");
    }


    @Test
    public void querySamplerReturnsCorrectNumberOfQueries() {
        try (GraknClient client = new GraknClient(server.grpcUri());
             GraknClient.Session session = client.session(testKeyspace)) {

            int queriesToSample = 100;
            int queriesToGenerate = 2000;
            List<VectorisedQuery> queries = QuerySampler.querySampleKMeans(session, queriesToGenerate, queriesToSample, 2);
            assertEquals(queries.size(), queriesToGenerate);
            for (VectorisedQuery query : queries) {
                assertNotNull(query);
                assertNotNull(query.graqlQuery);
                System.out.println(query.graqlQuery);
            }
        }
    }
}