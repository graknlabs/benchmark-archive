package grakn.benchmark.metric.test;

import grakn.benchmark.metric.Assortativity;
import grakn.benchmark.metric.StandardGraphProperties;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

public class AssortativityIT {

    @Test
    public void standardBinaryGraphAssortativity() throws IOException {
        Path edgeListFilePath = Paths.get("/Users/joshua/Documents/benchmark/metric/test/binaryGraph.csv");
        StandardGraphProperties graphProperties = new StandardGraphProperties(edgeListFilePath, ',');
        double computedAssortativity = Assortativity.computeAssortativity(Assortativity.jointDegreeOccurrence(graphProperties));
        double correctAssortativity = -0.38888888888888995;
        double allowedDeviation = 0.000001;
        assertEquals(correctAssortativity, computedAssortativity, allowedDeviation);
    }

    @Test
    public void standardUnaryBinaryGraphAssortativity() throws IOException {
        Path edgeListFilePath = Paths.get("/Users/joshua/Documents/benchmark/metric/test/unaryBinaryGraph.csv");
        StandardGraphProperties graphProperties = new StandardGraphProperties(edgeListFilePath, ',');
        double computedAssortativity = Assortativity.computeAssortativity(Assortativity.jointDegreeOccurrence(graphProperties));
        double correctAssortativity = -0.2767857142857146;
        double allowedDeviation = 0.000001;
        assertEquals(correctAssortativity, computedAssortativity, allowedDeviation);
    }
}
