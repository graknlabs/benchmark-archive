package grakn.benchmark.metric.test;

import grakn.benchmark.metric.DegreeDistribution;
import grakn.benchmark.metric.StandardGraphProperties;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertArrayEquals;

public class DegreeDistributionIT {

    @Test
    public void standardBinaryGraphToPercentiles() throws IOException {
        Path edgeListFilePath = Paths.get("/Users/joshua/Documents/benchmark/metric/test/binaryGraph.csv");
        // degree distribution: [1, 1, 2, 2, 2, 2, 2, 2, 3, 3]
        // compressed degree distribution [0th, 20th, 50th, 70th, 100th]: [1, 2, 2, 2, 3]

        StandardGraphProperties graphProperties = new StandardGraphProperties(edgeListFilePath, ',');
        double[] percentiles = new double[] {0, 20, 50, 70, 100};
        long[] discreteDegreeDistribution = DegreeDistribution.discreteDistribution(graphProperties, percentiles);
        long[] correctDegreeDistribution = new long[] {1, 2, 2, 2, 3};
        assertArrayEquals(correctDegreeDistribution, discreteDegreeDistribution);
    }

    @Test
    public void unaryBinaryGraphToPercentiles() throws IOException {

        // TODO



        Path edgeListFilePath = Paths.get("/Users/joshua/Documents/benchmark/metric/test/unaryBinaryGraph.csv");
        StandardGraphProperties graphProperties = new StandardGraphProperties(edgeListFilePath, ',');
        double[] percentiles = new double[] {0, 20, 50, 70, 100};
        long[] discreteDegreeDistribution = DegreeDistribution.discreteDistribution(graphProperties, percentiles);
        long[] correctDegreeDistribution = new long[] {1, 2, 2, 2, 3};
        assertArrayEquals(correctDegreeDistribution, discreteDegreeDistribution);
    }
}
