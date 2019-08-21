package grakn.benchmark.querygen.util;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Arguments {

    public final static String GRAKN_URI = "grakn-uri";
    public final static String KEYSPACE_ARGUMENT = "keyspace";


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
                .desc("Specific keyspace to utilize")
                .type(String.class)
                .build();

        Options options = new Options();
        options.addOption(graknAddressOption);
        options.addOption(keyspaceOption);
        return options;
    }

}
