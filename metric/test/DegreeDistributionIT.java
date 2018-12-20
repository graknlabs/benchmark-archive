package grakn.benchmark.metric.test;

import grakn.benchmark.metric.DegreeDistribution;
import grakn.benchmark.metric.GraknGraphProperties;
import grakn.benchmark.metric.StandardGraphProperties;
import grakn.core.GraknTxType;
import grakn.core.Keyspace;
import grakn.core.client.Grakn;
import grakn.core.util.SimpleURI;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;

public class DegreeDistributionIT {

    @Test
    public void standardBinaryGraphToPercentiles() throws IOException {
        Path edgeListFilePath = Paths.get("metric/test/binaryGraph.csv");
        // degree distribution: [1, 1, 2, 2, 2, 2, 2, 2, 3, 3]
        // compressed degree distribution [0th, 20th, 50th, 70th, 100th]: [1, 2, 2, 2, 3]

        StandardGraphProperties graphProperties = new StandardGraphProperties(edgeListFilePath, ',');
        double[] percentiles = new double[] {0, 20, 50, 70, 100};
        long[] discreteDegreeDistribution = DegreeDistribution.discreteDistribution(graphProperties, percentiles);
        long[] correctDegreeDistribution = new long[] {1, 2, 2, 2, 3};
        assertArrayEquals(correctDegreeDistribution, discreteDegreeDistribution);
    }

    /**
     * Test computing degree distribution when there are self-edges present
     * This test uses a standard graph from a CSV with several self edges, that should each count as degree + 2
     * @throws IOException
     */
    @Test
    public void standardUnaryBinaryGraphToPercentiles() throws IOException {
        Path edgeListFilePath = Paths.get("metric/test/unaryBinaryGraph.csv");
        StandardGraphProperties graphProperties = new StandardGraphProperties(edgeListFilePath, ',');
        double[] percentiles = new double[] {0, 20, 50, 70, 100};
        long[] discreteDegreeDistribution = DegreeDistribution.discreteDistribution(graphProperties, percentiles);
        long[] correctDegreeDistribution = new long[] {1, 2, 2, 3, 4};
        assertArrayEquals(correctDegreeDistribution, discreteDegreeDistribution);
    }


    @Test
    public void graknBinaryGraphToPercentiles() {
        Grakn client = new Grakn(new SimpleURI("localhost:48555"));

        // define basic schema
        String keyspaceName = "binary_graph_degree_dist_it";
        Grakn.Session session = client.session(Keyspace.of(keyspaceName));
        Grakn.Transaction tx = session.transaction(GraknTxType.WRITE);
        List<?> answer = tx.graql().parse("define vertex sub entity, plays endpt; edge sub relationship, relates endpt;").execute();
        tx.commit();

        // insert same data as `binaryGraph.csv`
        tx = session.transaction(GraknTxType.WRITE);
        answer = tx.graql().parse("insert" +
                "$v1 isa vertex; $v2 isa vertex; $v3 isa vertex; $v4 isa vertex; $v5 isa vertex; $v6 isa vertex;" +
                "$v7 isa vertex; $v8 isa vertex; $v9 isa vertex; $v10 isa vertex; " +
                "(endpt: $v1, endpt: $v2) isa edge; " +
                "(endpt: $v1, endpt: $v3) isa edge; " +
                "(endpt: $v3, endpt: $v4) isa edge; " +
                "(endpt: $v6, endpt: $v4) isa edge; " +
                "(endpt: $v6, endpt: $v5) isa edge; " +
                "(endpt: $v6, endpt: $v7) isa edge; " +
                "(endpt: $v8, endpt: $v7) isa edge; " +
                "(endpt: $v8, endpt: $v9) isa edge; " +
                "(endpt: $v8, endpt: $v10) isa edge; " +
                "(endpt: $v9, endpt: $v10) isa edge; ").execute();
        tx.commit();

        GraknGraphProperties graphProperties = new GraknGraphProperties("localhost:48555", keyspaceName);

        // This is the same process are with graphs read from file
        double[] percentiles = new double[]{0, 20, 50, 70, 100};
        long[] discreteDegreeDistribution = DegreeDistribution.discreteDistribution(graphProperties, percentiles);
        long[] correctDegreeDistribution = new long[]{1, 2, 2, 2, 3};
        assertArrayEquals(correctDegreeDistribution, discreteDegreeDistribution);

        client.keyspaces().delete(Keyspace.of(keyspaceName));
    }


    @Test
    public void graknUnaryBinaryGraphToPercentiles() {
        Grakn client = new Grakn(new SimpleURI("localhost:48555"));

        // define basic schema
        String keyspaceName = "binary_graph_degree_dist_it";
        Grakn.Session session = client.session(Keyspace.of(keyspaceName));
        Grakn.Transaction tx = session.transaction(GraknTxType.WRITE);
        List<?> answer = tx.graql().parse("define vertex sub entity, plays endpt; edge sub relationship, relates endpt;").execute();
        tx.commit();

        // insert same data as `unaryBinaryGraph.csv`
        tx = session.transaction(GraknTxType.WRITE);
        answer = tx.graql().parse("insert" +
                "$v1 isa vertex; $v2 isa vertex; $v3 isa vertex; $v4 isa vertex; $v5 isa vertex; $v6 isa vertex;" +
                "$v7 isa vertex; $v8 isa vertex; $v9 isa vertex; $v10 isa vertex; " +
                "(endpt: $v1, endpt: $v1) isa edge; " + // self-loop
                "(endpt: $v1, endpt: $v2) isa edge; " +
                "(endpt: $v1, endpt: $v3) isa edge; " +
                "(endpt: $v3, endpt: $v4) isa edge; " +
                "(endpt: $v4, endpt: $v4) isa edge; " + // self-loop
                "(endpt: $v6, endpt: $v4) isa edge; " +
                "(endpt: $v6, endpt: $v5) isa edge; " +
                "(endpt: $v6, endpt: $v7) isa edge; " +
                "(endpt: $v8, endpt: $v7) isa edge; " +
                "(endpt: $v8, endpt: $v9) isa edge; " +
                "(endpt: $v8, endpt: $v10) isa edge; " +
                "(endpt: $v9, endpt: $v10) isa edge; ").execute();
        tx.commit();

        GraknGraphProperties graphProperties = new GraknGraphProperties("localhost:48555", keyspaceName);

        // This is the same process are with graphs read from file
        double[] percentiles = new double[]{0, 20, 50, 70, 100};
        long[] discreteDegreeDistribution = DegreeDistribution.discreteDistribution(graphProperties, percentiles);
        long[] correctDegreeDistribution = new long[]{1, 2, 2, 3, 4};
        assertArrayEquals(correctDegreeDistribution, discreteDegreeDistribution);

        client.keyspaces().delete(Keyspace.of(keyspaceName));
    }
}
