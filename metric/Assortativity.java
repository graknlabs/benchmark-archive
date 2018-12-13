package grakn.benchmark.metric;


import org.nd4j.linalg.factory.Nd4j;

public class Assortativity {

    public static double computeAssortativity(GraphProperties properties) {

        long maxDegree = properties.maxDegree();

        // create a maxDegree x maxDegree matrix
        // populate it with the stream of joined Degrees
        // calculate assortativity

        return 0.0;
    }
}
