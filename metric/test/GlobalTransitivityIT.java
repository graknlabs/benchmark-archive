package grakn.benchmark.metric.test;

import grakn.benchmark.metric.GlobalTransitivity;
import grakn.benchmark.metric.StandardGraphProperties;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

public class GlobalTransitivityIT {

    @Test
    public void standardBinaryGraphTransitivity() throws IOException {
        Path edgeListFilePath = Paths.get("/Users/joshua/Documents/benchmark/metric/test/binaryGraph.csv");
        StandardGraphProperties graphProperties = new StandardGraphProperties(edgeListFilePath, ',');
        double computedTransitivity = GlobalTransitivity.computeTransitivity(graphProperties);
        double correctTransitivity = 0.25;
        double allowedDeviationFraction = 0.0000001;
        assertEquals(correctTransitivity, computedTransitivity, allowedDeviationFraction * correctTransitivity);
    }

    /**
     * Test computing degree distribution when there are self-edges present
     * This test uses a standard graph from a CSV with several self edges, that should each count as degree + 2
     * @throws IOException
     */
    @Test
    public void standardUnaryBinaryGraphTransitivity() throws IOException {
        Path edgeListFilePath = Paths.get("/Users/joshua/Documents/benchmark/metric/test/unaryBinaryGraph.csv");
        StandardGraphProperties graphProperties = new StandardGraphProperties(edgeListFilePath, ',');
        double computedTransitivity = GlobalTransitivity.computeTransitivity(graphProperties);
        double correctTransitivity = 0.25;
        double allowedDeviationFraction = 0.0000001;
        assertEquals(correctTransitivity, computedTransitivity, allowedDeviationFraction * correctTransitivity);
    }

}
