package metric;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.nio.file.Path;
import java.nio.file.Paths;

public class MetricMeasurement {


    public static void main(String[] args) {

        Option graknAddress = Option.builder("u")
                .longOpt("uri")
                .hasArg(true)
                .desc("Address of the grakn cluster (default: localhost:48555)")
                .required(false)
                .type(String.class)
                .build();

        Option graknKeyspace = Option.builder("k")
                .longOpt("keyspace")
                .hasArg(true)
                .desc("Grakn keyspace to analyse")
                .required(false)
                .type(String.class)
                .build();

        Option standardEdgeList = Option.builder("e")
                .longOpt("edgelist")
                .hasArg(true)
                .desc("[ALT to keyspace] Path to a CSV file listing edges representing a standard unary/binary edge graph")
                .required(false)
                .type(String.class)
                .build();
        Option standardVertexList = Option.builder("v")
                .longOpt("vertexlist")
                .hasArg(true)
                .desc("[ALT to keyspace] Path to file listing vertices representing standard graph, one per line")
                .required(false)
                .type(String.class)
                .build();

        Options options = new Options();
        options.addOption(graknKeyspace);
        options.addOption(standardEdgeList);
        options.addOption(standardVertexList);

        CommandLineParser parser = new DefaultParser();
        CommandLine arguments;
        try {
            arguments = parser.parse(options, args);
        } catch (ParseException e) {
            (new HelpFormatter()).printHelp("Benchmarking options", options);
            throw new RuntimeException(e.getMessage());
        }

        if (arguments.hasOption('u') && arguments.hasOption('k')) {
            graknMetrics(arguments.getOptionValue('u'), arguments.getOptionValue('k'));
        } else if (arguments.hasOption('e')) {
            String edgelistFile = arguments.getOptionValue('e');
            Path edgelistPath = Paths.get(edgelistFile);
            Path vertexlistPath = arguments.hasOption('v') ? Paths.get(arguments.getOptionValue('v')) : null;
            standardGraphMetrics(edgelistPath, vertexlistPath);
        } else {
            System.out.println("Either require --uri and --keyspace to be set, or --edgelist (+optionally --vertexlist) to be set");
        }
    }

    private static void graknMetrics(String uri, String keyspace) {

    }

    private static void standardGraphMetrics(Path edgelistPath, Path vertexlistPath) {

    }


    private static double[] discreteDegreeDistribution(double[] percentiles) {

        return null;
    }

    private static double assortativity() {

        return 0.0;
    }

    private static double transitivity() {

        return 0.0;
    }
}
