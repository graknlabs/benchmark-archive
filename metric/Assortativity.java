package grakn.benchmark.metric;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

public class Assortativity {

    public static double computeAssortativity(INDArray jointDegreeOccurrence) {

        if (Math.abs(jointDegreeOccurrence.sumNumber().doubleValue()  - 1.0) > 0.00001) {
            throw new RuntimeException("Assortativity requires normalized joint degree occurrence matrix");
        }

        // calculate degree assortativity

        // method following networkx's internal implementation
        // generate the full matrix of degree -- degree connectivity counts
        // ie. joint probability distribution of connected vertices' degrees
        // normalize this across the whole matrix/distribution (not this implicitly counts edges twice unless its a loop)
        // then using this, compute required q_k distribution by
        // summing columns or rows (symmetric matrix for undirected edge case we have)
        //
        // can use this matrix & q_k to compute variance(q), and the required sum in r
        // for reference, follow from
        // https://networkx.github.io/documentation/stable/_modules/networkx/algorithms/assortativity/correlation.html//degree_assortativity_coefficient

        long maxDegree = jointDegreeOccurrence.shape()[0];
        INDArray enumeratedDegrees = Nd4j.arange(maxDegree);

        // q follows from the property that the sum of any column j (or row since symmetric) = q_j
        INDArray q = jointDegreeOccurrence.sum(0); // sum along first dimension (rows or cols is interchangible, symmetric)

        double qMean = q.mmul(enumeratedDegrees.transpose()).getDouble(0);
        INDArray diffs = enumeratedDegrees.sub(qMean);
        INDArray diffsSquared = diffs.mul(diffs); // (a-b)^2 elementise
        double qVar = q.mul(diffsSquared).sumNumber().doubleValue();

        double r = 0.0;
        for (long jDegree= 0; jDegree < maxDegree; jDegree++) {
            double q_j = q.getDouble(jDegree);
            for (long kDegree = 0; kDegree< maxDegree; kDegree++) {
                double q_k = q.getDouble(kDegree);
                double jointDistributionValue = jointDegreeOccurrence.getDouble(jDegree, kDegree);
                r += jDegree * kDegree * (jointDistributionValue - q_j*q_k);
            }
        }
        return r/qVar;
    }

    public static INDArray jointDegreeOccurrence(GraphProperties properties) {

        long maxDegree = properties.maxDegree();

        // create a maxDegree x maxDegree matrix
        // populate it with the stream of joined Degrees

        INDArray jointDegreeOccurrence = Nd4j.zeros(maxDegree+1, maxDegree+1);

        // jointDegreeOccurrence[deg1][deg2] += 1 for each deg1, deg2
        properties.connectedVertexDegrees().forEach(
                degrees ->
                        jointDegreeOccurrence.putScalar(
                                new long[] {degrees.getFirst(), degrees.getSecond()},
                                jointDegreeOccurrence.getDouble(degrees.getFirst(), degrees.getSecond()) + 1
                        )
        );

        return jointDegreeOccurrence.div(jointDegreeOccurrence.sumNumber());
    }
}
