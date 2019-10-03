package grakn.benchmark.querygen.util;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.nio.file.Path;

public class Arguments {

    public final static String GRAKN_URI = "grakn-uri";
    public final static String KEYSPACE_ARGUMENT = "keyspace";
    public final static String KMEANS_SAMPLING_ARGUMENT = "kmeans";
    public final static String GRIDDED_SAMPLING_ARGUMENT = "gridded";
    public final static String GENERATE_ARGUMENT = "generate";
    public final static String SAMPLE_ARGUMENT = "sample";
    public final static String OUTPUT_ARGUMENT = "output";



    public static CommandLine parse(String[] args) throws ParseException {
        Options options = buildOptions();
        CommandLineParser parser = new DefaultParser();
        CommandLine arguments = parser.parse(options, args);
        return arguments;
    }

    private static Options buildOptions() {
        Option graknAddressOption = Option.builder("u")
                .longOpt(GRAKN_URI)
                .hasArg(true)
                .desc("Address of the grakn cluster (default: localhost:48555)")
                .required(false)
                .type(String.class)
                .build();

        Option keyspaceOption = Option.builder("k")
                .longOpt(KEYSPACE_ARGUMENT)
                .required(true)
                .hasArg(true)
                .desc("Specific keyspace to utilise")
                .type(String.class)
                .build();

        Option kMeansSamplingOption = Option.builder("m")
                .longOpt(KMEANS_SAMPLING_ARGUMENT)
                .required(false)
                .hasArg(false)
                .desc("Sample queries using K Means")
                .build();

        Option griddedSamplingOption = Option.builder("r")
                .longOpt(GRIDDED_SAMPLING_ARGUMENT)
                .required(false)
                .hasArg(false)
                .desc("Sample queries using K Means")
                .build();

        Option generateOption = Option.builder("g")
                .longOpt(GENERATE_ARGUMENT)
                .required(true)
                .hasArg(true)
                .desc("Number of queries to generate before sampling")
                .type(Integer.class)
                .build();

        Option samplesOption = Option.builder("s")
                .longOpt(SAMPLE_ARGUMENT)
                .required(true)
                .hasArg(true)
                .desc("Number of queries to sample down to")
                .type(Integer.class)
                .build();

        Option destinationDirectoryOption = Option.builder("d")
                .longOpt(OUTPUT_ARGUMENT)
                .required(true)
                .hasArg(true)
                .desc("Absolute path to directory to which the queries will be written in generated_queries_n.gql")
                .type(Path.class)
                .build();

        Options options = new Options();
        options.addOption(graknAddressOption);
        options.addOption(keyspaceOption);
        options.addOption(kMeansSamplingOption);
        options.addOption(griddedSamplingOption);
        options.addOption(generateOption);
        options.addOption(samplesOption);
        options.addOption(destinationDirectoryOption);
        return options;
    }

}
