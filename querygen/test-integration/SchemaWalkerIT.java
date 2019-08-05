package grakn.benchmark.querygen;

import grakn.client.GraknClient;
import grakn.core.concept.type.Type;
import grakn.core.rule.GraknTestServer;
import graql.lang.Graql;
import graql.lang.query.GraqlQuery;
import org.hamcrest.CoreMatchers;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class SchemaWalkerIT {

    private static final String testKeyspace = "schemawalker_test";

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
    public static void walkSubRetrievesDifferentSubtypes() {
        GraknClient client = new GraknClient(server.grpcUri());
        GraknClient.Session session = client.session(testKeyspace);
        GraknClient.Transaction tx = session.transaction().write();

        Type rootThing = tx.getMetaConcept();
        Random random = new Random(0);

        List<Type> subTypes = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            subTypes.add(SchemaWalker.walkSub(rootThing, random));
        }

        assertThat(subTypes, CoreMatchers.everyItem(CoreMatchers.is(rootThing)));
    }
}
