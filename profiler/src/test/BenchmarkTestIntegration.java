package grakn.benchmark.profiler.test;

import grakn.benchmark.profiler.BootupException;
import grakn.benchmark.profiler.GraknBenchmark;
import grakn.benchmark.profiler.util.BenchmarkArguments;
import grakn.core.client.GraknClient;
import grakn.core.graql.answer.ConceptMap;
import grakn.core.graql.query.Graql;
import grakn.core.server.Transaction;
import org.apache.commons.cli.CommandLine;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import static grakn.core.graql.query.Graql.type;
import static grakn.core.graql.query.Graql.var;

public class BenchmarkTestIntegration {
    private final static Path WEB_CONTENT_CONFIG_PATH = Paths.get("test/resources/web_content/web_content_config_test.yml");

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();
    private GraknClient client;
    private GraknClient.Session session;
    private String keyspace;

    @Before
    public void setUp() {
        client = new GraknClient("localhost:48555");
        String uuid = UUID.randomUUID().toString().substring(0, 30).replace("-", "");
        keyspace = "test_" + uuid;
        session = client.session(keyspace);
    }

    @After
    public void tearDown() {
        client.keyspaces().delete(keyspace);
        session.close();
    }

    @Test
    public void whenSchemaExistsInKeyspace_throwException() {

        try (GraknClient.Transaction tx = session.transaction(Transaction.Type.WRITE)) {
            List<ConceptMap> answer = tx.execute(Graql.define(type("person").sub("entity")));
            tx.commit();
        }

        expectedException.expect(BootupException.class);
        expectedException.expectMessage("not empty, contains a schema");
        String[] args = new String[]{"--config", WEB_CONTENT_CONFIG_PATH.toAbsolutePath().toString(), "--keyspace", keyspace.toString(), "--execution-name", "testing"};
        CommandLine commandLine = BenchmarkArguments.parse(args);
        GraknBenchmark graknBenchmark = new GraknBenchmark(commandLine);
        graknBenchmark.start();
    }

    @Test
    public void whenDataExistsInKeyspace_throwException() {

        try (GraknClient.Transaction tx = session.transaction(Transaction.Type.WRITE)) {
            List<ConceptMap> answer = tx.execute(Graql.define(type("person").sub("entity")));
            answer = tx.execute(Graql.insert(var("x").isa("person")));
            tx.commit();
        }

        expectedException.expect(BootupException.class);
        expectedException.expectMessage("not empty, contains concept instances");
        String[] args = new String[] {"--config", WEB_CONTENT_CONFIG_PATH.toAbsolutePath().toString(), "--keyspace", keyspace.toString(), "--execution-name", "testing"};
        CommandLine commandLine = BenchmarkArguments.parse(args);
        GraknBenchmark graknBenchmark = new GraknBenchmark(commandLine);
        graknBenchmark.start();
    }
}