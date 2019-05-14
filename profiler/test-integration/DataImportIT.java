package grakn.benchmark.profiler;

import grakn.benchmark.common.configuration.parse.BenchmarkArguments;
import grakn.client.GraknClient;
import grakn.core.concept.answer.Numeric;
import graql.lang.Graql;
import org.apache.commons.cli.CommandLine;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import static junit.framework.TestCase.assertTrue;

public class DataImportIT {
    private final static Path WEB_CONTENT_DATA_IMPORT_CONFIG_PATH = Paths.get("profiler/test-integration/resources/web_content/web_content_config_data_import.yml");

    private GraknClient client;
    private GraknClient.Session session;
    private String keyspace;

    @Before
    public void setUp() {
        String uri = "localhost:48555";
        client = new GraknClient(uri);
        String uuid = UUID.randomUUID().toString().substring(0, 30).replace("-", "");
        keyspace = "test_" + uuid;
        session = client.session(keyspace);
    }

    @After
    public void tearDown() {
        session.close();
        client.keyspaces().delete(keyspace);
        client.close();
    }

    @Test
    public void whenUsingDataImportFile_graknContainsData() {
        String[] args = new String[] {
                "--config", WEB_CONTENT_DATA_IMPORT_CONFIG_PATH.toString(),
                "--keyspace", keyspace,
                "--execution-name", "testing",
                "--" + BenchmarkArguments.LOAD_SCHEMA_ARGUMENT,
                "--" + BenchmarkArguments.STATIC_DATA_IMPORT_ARGUMENT};
        CommandLine commandLine = BenchmarkArguments.parse(args);
        GraknBenchmark graknBenchmark = new GraknBenchmark(commandLine);
        graknBenchmark.start();

        try (GraknClient.Transaction tx = session.transaction().read()) {
            List<Numeric> answer = tx.execute(Graql.parse("match $x isa thing; get; count;").asGetAggregate());
            assertTrue(answer.get(0).number().intValue() > 0);
        }

    }
}
