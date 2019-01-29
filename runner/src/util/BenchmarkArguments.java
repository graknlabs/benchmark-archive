package grakn.benchmark.runner.util;

import grakn.benchmark.runner.exception.BootupException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Helper class used to parse console arguments into predefined options
 */
public class BenchmarkArguments {

    public final static String CONFIG_ARGUMENT = "config";
    public final static String GRAKN_URI = "grakn-uri";
    public final static String KEYSPACE_ARGUMENT = "keyspace";
    public final static String NO_DATA_GENERATION_ARGUMENT = "no-data-generation";
    public final static String EXECUTION_NAME_ARGUMENT = "execution-name";
    public final static String ELASTIC_URI = "elastic-uri";

    public static CommandLine parse(String[] args) {
        Options options = buildOptions();
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine arguments = parser.parse(options, args);
            return arguments;
        } catch (ParseException e) {
            throw new BootupException(e.getMessage(), e.getCause());
        }
    }

    private static Options buildOptions() {
        Option configFileOption = Option.builder("c")
                .longOpt(CONFIG_ARGUMENT)
                .hasArg(true)
                .desc("Benchmarking YAML file (required)")
                .required(true)
                .type(String.class)
                .build();
        Option executionNameOption = Option.builder("n")
                .longOpt(EXECUTION_NAME_ARGUMENT)
                .hasArg(true)
                .required(true)
                .desc("Name for specific execution, this label is passed through to the storage backend")
                .type(String.class)
                .build();
        Option graknAddressOption = Option.builder("u")
                .longOpt(GRAKN_URI)
                .hasArg(true)
                .desc("Address of the grakn cluster (default: localhost:48555)")
                .required(false)
                .type(String.class)
                .build();

        Option keyspaceOption = Option.builder("k")
                .longOpt(KEYSPACE_ARGUMENT)
                .required(false)
                .hasArg(true)
                .desc("Specific keyspace to utilize (default: `name` in config yaml")
                .type(String.class)
                .build();
        Option noDataGenerationOption = Option.builder("ng")
                .longOpt(NO_DATA_GENERATION_ARGUMENT)
                .required(false)
                .desc("Disable data generation")
                .type(Boolean.class)
                .build();
        Option elasticsearchAddressOption = Option.builder("e")
                .longOpt(ELASTIC_URI)
                .hasArg(true)
                .desc("Address of the elastic search server (default: localhost:9200")
                .required(false)
                .type(String.class)
                .build();
        Options options = new Options();
        options.addOption(configFileOption);
        options.addOption(graknAddressOption);
        options.addOption(keyspaceOption);
        options.addOption(noDataGenerationOption);
        options.addOption(executionNameOption);
        options.addOption(elasticsearchAddressOption);
        return options;
    }
}
