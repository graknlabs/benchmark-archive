package grakn.benchmark.common.configuration;

import grakn.benchmark.profiler.GraknBenchmark;
import org.apache.commons.cli.CommandLine;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;



/**
 *
 */
public class ConfigurationTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void whenProvidingAbsolutePathToNonExistingConfig_throwException() {
        String[] args = new String[]{"--config", "nonexistingpath", "--execution-name", "grakn-benchmark-test"};
        CommandLine commandLine = BenchmarkArguments.parse(args);
        expectedException.expect(ConfigurationException.class);
        expectedException.expectMessage("The provided config file [nonexistingpath] does not exist");
        GraknBenchmark graknBenchmark = new GraknBenchmark(commandLine);
    }

    @Test
    public void whenConfigurationArgumentNotProvided_throwException() {
        expectedException.expect(ConfigurationException.class);
        expectedException.expectMessage("Missing required option: c");
        GraknBenchmark graknBenchmark = new GraknBenchmark(BenchmarkArguments.parse(new String[] {"--execution-name", "grakn-benchmark-test"}));
    }

    @Test
    public void whenExecutionNameArgumentNotProvided_throwException() {
        expectedException.expect(ConfigurationException.class);
        expectedException.expectMessage("Missing required option: n");
        GraknBenchmark graknBenchmark = new GraknBenchmark(BenchmarkArguments.parse(new String[] {"--config", "web_content_config_test.yml"}));
    }
}