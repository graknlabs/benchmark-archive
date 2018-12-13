package grakn.benchmark.metric;

import com.sun.corba.se.impl.orbutil.graph.Graph;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import grakn.benchmark.metric.Assortativity;

public class MetricMeasurement {

    public static void main(String[] args) {
        // TODO passthrough for testing only
        mainImpl(args);
    }

    public static double[] mainImpl(String[] args) {

        // TODO turn this into CommandLineArgParser
        String standardGraphEdgeList = args[0];
        double percentile1 = Double.parseDouble(args[1]);
        double percentile2 = Double.parseDouble(args[2]);
        double percentile3 = Double.parseDouble(args[3]);
        double percentile4 = Double.parseDouble(args[4]);
        double percentile5 = Double.parseDouble(args[5]);
        double[] percentiles = new double[] {percentile1, percentile2, percentile3, percentile4, percentile5};

        File edgelist = Paths.get(standardGraphEdgeList).toFile();

        GraphProperties properties = new StandardGraphProperties(edgelist);


        double[] metrics = new double[percentiles.length + 2];

        metrics[0] = Assortativity.computeAssortativity(properties);
        metrics[1] = GlobalTransitivity.computeTransitivity(properties);
        double[] discreteDistribution = DegreeDistribution.discreteDistribution(properties, percentiles);

        metrics[2] = discreteDistribution[0];
        metrics[3] = discreteDistribution[1];
        metrics[4] = discreteDistribution[2];
        metrics[5] = discreteDistribution[3];
        metrics[6]  = discreteDistribution[4];
        return metrics;
    }


//
//    public static void main(String[] args) {
//
//        Option graknUri = Option.builder("u")
//                .longOpt("uri")
//                .hasArg(true)
//                .desc("Address of the grakn cluster (default: localhost:48555)")
//                .required(false)
//                .type(String.class)
//                .build();
//
//        Option graknKeyspace = Option.builder("k")
//                .longOpt("keyspace")
//                .hasArg(true)
//                .desc("Grakn keyspace to analyse")
//                .required(false)
//                .type(String.class)
//                .build();
//
//        Option standardEdgeList = Option.builder("e")
//                .longOpt("edgelist")
//                .hasArg(true)
//                .desc("[ALT to keyspace] Path to a CSV file listing edges representing a standard unary/binary edge graph")
//                .required(false)
//                .type(String.class)
//                .build();
//
//        Option standardVertexList = Option.builder("v")
//                .longOpt("vertexlist")
//                .hasArg(true)
//                .desc("[ALT to keyspace] Path to file listing vertices representing standard graph, one per line")
//                .required(false)
//                .type(String.class)
//                .build();
//
//        Options options = new Options();
//        options.addOption(graknUri);
//        options.addOption(graknKeyspace);
//        options.addOption(standardEdgeList);
//        options.addOption(standardVertexList);
//
//        CommandLineParser parser = new DefaultParser();
//        CommandLine arguments;
//        try {
//            arguments = parser.parse(options, args);
//        } catch (ParseException e) {
//            (new HelpFormatter()).printHelp("Benchmarking options", options);
//            throw new RuntimeException(e.getMessage());
//        }
//
//        GraphProperties properties;
//        if (arguments.hasOption('u') && arguments.hasOption('k')) {
//            properties = graknProperties(arguments.getOptionValue('u'), arguments.getOptionValue('k'));
//        } else if (arguments.hasOption('e')) {
//            String edgelistFile = arguments.getOptionValue('e');
//            Path edgelistPath = Paths.get(edgelistFile);
//            Path vertexlistPath = arguments.hasOption('v') ? Paths.get(arguments.getOptionValue('v')) : null;
//            properties = standardGraphProperties(edgelistPath, vertexlistPath);
//        } else {
//            throw new RuntimeException("Either require --uri and --keyspace to be set, or --edgelist (+optionally --vertexlist) to be set");
//        }
//
//        computeMetrics(properties);
//    }
//
//    private static void computeMetrics(GraphProperties properties) {
//
//        double[] percentiles = new double[] {1.0, 25.0, 50.0, 75.0, 100.0};
//        double[] distribution = discreteDegreeDistribution(properties, percentiles);
//
//        double assortativity = assortativity(properties);
//
//        double transitivity = transitivity(properties);
//    }
//
//    private static GraphProperties graknProperties(String uri, String keyspace) {
//        GraknGraphProperties graknGraphProperties = new GraknGraphProperties(uri, keyspace);
//        return graknGraphProperties;
//    }
//
//    private static GraphProperties standardGraphProperties(Path edgelistPath, Path vertexlistPath) {
//        StandardGraphProperties standardGraphProperties;
//        if (vertexlistPath == null) {
//            standardGraphProperties = new StandardGraphProperties(edgelistPath.toFile());
//        } else {
//            standardGraphProperties = new StandardGraphProperties(edgelistPath.toFile(), vertexlistPath.toFile());
//        }
//        return standardGraphProperties;
//    }
//
//
//    private static double[] discreteDegreeDistribution(GraphProperties graphProperties, double[] percentiles) {
//
//        return null;
//    }
//
//    private static double assortativity(GraphProperties graphProperties) {
//
//        return 0.0;
//    }
//
//    private static double transitivity(GraphProperties graphProperties) {
//
//        return 0.0;
//    }
}
