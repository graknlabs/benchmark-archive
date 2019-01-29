package grakn.benchmark.runner;

import grakn.benchmark.runner.exception.BootupException;
import grakn.benchmark.runner.util.BenchmarkArguments;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.apache.commons.cli.CommandLine;

import java.nio.file.Path;
import java.nio.file.Paths;


/**
 *
 */
public class GraknBenchmarkTest {

    private final static Path WEB_CONTENT_CONFIG_PATH = Paths.get("runner/test/resources/web_content/web_content_config_test.yml");

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void whenProvidingAbsolutePathToExistingConfig_benchmarkShouldStart() {
        String[] args = new String[]{"--config", WEB_CONTENT_CONFIG_PATH.toAbsolutePath().toString(), "--execution-name", "grakn-benchmark-test"};
        CommandLine commandLine = BenchmarkArguments.parse(args);
        GraknBenchmark graknBenchmark = new GraknBenchmark(commandLine);
    }

    @Test
    public void whenProvidingRelativePathToExistingConfig_benchmarkShouldStart() {
        String[] args = new String[]{"--config", "web_content_config_test.yml", "--execution-name", "grakn-benchmark-test"};
        System.setProperty("working.dir", WEB_CONTENT_CONFIG_PATH.getParent().toString());
        CommandLine commandLine = BenchmarkArguments.parse(args);
        GraknBenchmark graknBenchmark = new GraknBenchmark(commandLine);
    }

    @Test
    public void whenProvidingAbsolutePathToNonExistingConfig_throwException() {
        String[] args = new String[]{"--config", "nonexistingpath", "--execution-name", "grakn-benchmark-test"};
        CommandLine commandLine = BenchmarkArguments.parse(args);
        expectedException.expect(BootupException.class);
        expectedException.expectMessage("The provided config file [nonexistingpath] does not exist");
        GraknBenchmark graknBenchmark = new GraknBenchmark(commandLine);
    }

    @Test
    public void whenConfigurationArgumentNotProvided_throwException() {
        expectedException.expect(BootupException.class);
        expectedException.expectMessage("Missing required option: c");
        GraknBenchmark graknBenchmark = new GraknBenchmark(BenchmarkArguments.parse(new String[] {"--execution-name", "grakn-benchmark-test"}));
    }

    @Test
    public void whenExecutionNameArgumentNotProvided_throwException() {
        expectedException.expect(BootupException.class);
        expectedException.expectMessage("Missing required option: n");
        GraknBenchmark graknBenchmark = new GraknBenchmark(BenchmarkArguments.parse(new String[] {"--config", "web_content_config_test.yml"}));
    }
}