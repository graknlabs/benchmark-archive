package grakn.benchmark.runner;

import grakn.benchmark.runner.exception.BootupException;
import grakn.core.GraknTxType;
import grakn.core.Keyspace;
import grakn.core.client.Grakn;
import grakn.core.graql.answer.ConceptMap;
import grakn.core.util.SimpleURI;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import static grakn.core.graql.Graql.var;

public class SchemaManagerE2E {
    private final static Path WEB_CONTENT_CONFIG_PATH = Paths.get("runner/test/resources/web_content/web_content_config_test.yml");

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();
    private Grakn client;
    private Grakn.Session session;
    private Keyspace keyspace;

    @Before
    public void setUp() {
        client = new Grakn(new SimpleURI("localhost:48555"));
        String uuid = UUID.randomUUID().toString().substring(0, 30).replace("-", "");
        keyspace = Keyspace.of("test_" + uuid);
        session = client.session(keyspace);
    }

    @After
    public void tearDown() {
        client.keyspaces().delete(keyspace);
        session.close();
    }

    @Test
    public void whenSchemaExistsInKeyspace_ThrowException() {

        try (Grakn.Transaction tx = session.transaction(GraknTxType.WRITE)) {
            List<ConceptMap> answer = tx.graql().define(var("x").sub("entity").label("person")).execute();
            tx.commit();
        }

        expectedException.expect(BootupException.class);
        expectedException.expectMessage("not empty, contains a schema");
        GraknBenchmark graknBenchmark = new GraknBenchmark(new String[]{"--config", WEB_CONTENT_CONFIG_PATH.toAbsolutePath().toString(), "--keyspace", keyspace.toString()});
        graknBenchmark.start();
    }

    @Test
    public void whenDataExistsInKeyspace_ThrowException() {

        try (Grakn.Transaction tx = session.transaction(GraknTxType.WRITE)) {
            List<ConceptMap> answer = tx.graql().define(var("x").sub("entity").label("person")).execute();
            answer = tx.graql().insert(var("x").isa("person")).execute();
            tx.commit();
        }

        expectedException.expect(BootupException.class);
        expectedException.expectMessage("not empty, contains concept instances");
        GraknBenchmark graknBenchmark = new GraknBenchmark(new String[]{"--config", WEB_CONTENT_CONFIG_PATH.toAbsolutePath().toString(), "--keyspace", keyspace.toString()});
        graknBenchmark.start();
    }
}