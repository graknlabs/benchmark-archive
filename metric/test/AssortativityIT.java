package grakn.benchmark.metric.test;

import grakn.benchmark.metric.Assortativity;
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

import static org.junit.Assert.assertEquals;

public class AssortativityIT {

    @Test
    public void standardBinaryGraphAssortativity() throws IOException {
        Path edgeListFilePath = Paths.get("metric/test/binaryGraph.csv");
        StandardGraphProperties graphProperties = new StandardGraphProperties(edgeListFilePath, ',');
        double computedAssortativity = Assortativity.computeAssortativity(Assortativity.jointDegreeOccurrence(graphProperties));
        double correctAssortativity = -0.38888888888888995;
        double allowedDeviation = 0.000001;
        assertEquals(correctAssortativity, computedAssortativity, allowedDeviation);
    }

    @Test
    public void standardUnaryBinaryGraphAssortativity() throws IOException {
        Path edgeListFilePath = Paths.get("metric/test/unaryBinaryGraph.csv");
        StandardGraphProperties graphProperties = new StandardGraphProperties(edgeListFilePath, ',');
        double computedAssortativity = Assortativity.computeAssortativity(Assortativity.jointDegreeOccurrence(graphProperties));
        double correctAssortativity = -0.2767857142857146;
        double allowedDeviation = 0.000001;
        assertEquals(correctAssortativity, computedAssortativity, allowedDeviation);
    }

    @Test
    public void graknBinaryGraphAssortativity() {
        Grakn client = new Grakn(new SimpleURI("localhost:48555"));

        // define basic schema
        String keyspaceName = "assortativity_it";
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

        double computedAssortativity = Assortativity.computeAssortativity(Assortativity.jointDegreeOccurrence(graphProperties));
        double correctAssortativity = -0.38888888888888995;
        double allowedDeviation = 0.000001;
        client.keyspaces().delete(Keyspace.of(keyspaceName));
        assertEquals(correctAssortativity, computedAssortativity, allowedDeviation);
    }


    @Test
    public void graknUnaryBinaryGraphAssortativity() {
        Grakn client = new Grakn(new SimpleURI("localhost:48555"));

        // define basic schema
        String keyspaceName = "assortativity_it";
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

        double computedAssortativity = Assortativity.computeAssortativity(Assortativity.jointDegreeOccurrence(graphProperties));
        double correctAssortativity = -0.2767857142857146;
        double allowedDeviation = 0.000001;
        client.keyspaces().delete(Keyspace.of(keyspaceName));
        assertEquals(correctAssortativity, computedAssortativity, allowedDeviation);
    }
}
