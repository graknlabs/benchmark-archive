package grakn.benchmark.metric.test;

import grakn.benchmark.metric.GlobalTransitivity;
import grakn.benchmark.metric.GraknGraphProperties;
import grakn.benchmark.metric.StandardGraphProperties;
import grakn.core.client.GraknClient;
import graql.lang.Graql;
import graql.lang.query.GraqlDefine;
import graql.lang.query.GraqlInsert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class GlobalTransitivityIT {

    @Test
    public void standardBinaryGraphTransitivity() throws IOException {
        Path edgeListFilePath = Paths.get("metric/test/binaryGraph.csv");
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
        Path edgeListFilePath = Paths.get("metric/test/unaryBinaryGraph.csv");
        StandardGraphProperties graphProperties = new StandardGraphProperties(edgeListFilePath, ',');
        double computedTransitivity = GlobalTransitivity.computeTransitivity(graphProperties);
        double correctTransitivity = 0.25;
        double allowedDeviationFraction = 0.0000001;
        assertEquals(correctTransitivity, computedTransitivity, allowedDeviationFraction * correctTransitivity);
    }


    @Test
    public void graknBinaryGraphTransitivity() {
        GraknClient client = new GraknClient("localhost:48555");

        // define basic schema
        String keyspaceName = "transitivity_it";
        client.keyspaces().delete(keyspaceName);
        GraknClient.Session session = client.session(keyspaceName);
        GraknClient.Transaction tx = session.transaction().write();
        List<?> answer = tx.execute(Graql.<GraqlDefine>parse("define vertex sub entity, plays endpt; edge sub relation, relates endpt;"));
        tx.commit();

        // insert same data as `binaryGraph.csv`
        tx = session.transaction().write();
        answer = tx.execute(Graql.<GraqlInsert>parse("insert" +
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
                "(endpt: $v9, endpt: $v10) isa edge; "));
        tx.commit();

        GraknGraphProperties graphProperties = new GraknGraphProperties("localhost:48555", keyspaceName);

        double computedTransitivity = GlobalTransitivity.computeTransitivity(graphProperties);
        double correctTransitivity = 0.25;
        double allowedDeviationFraction = 0.0000001;
        client.keyspaces().delete(keyspaceName);
        session.close();
        assertEquals(correctTransitivity, computedTransitivity, allowedDeviationFraction * correctTransitivity);
    }


    @Test
    public void graknUnaryBinaryGraphToPercentiles() {
        GraknClient client = new GraknClient("localhost:48555");

        // define basic schema
        String keyspaceName = "transitivity_it";
        client.keyspaces().delete(keyspaceName);
        GraknClient.Session session = client.session(keyspaceName);
        GraknClient.Transaction tx = session.transaction().write();
        List<?> answer = tx.execute(Graql.<GraqlDefine>parse("define vertex sub entity, plays endpt; edge sub relation, relates endpt;"));
        tx.commit();

        // insert same data as `unaryBinaryGraph.csv`
        tx = session.transaction().write();
        answer = tx.execute(Graql.<GraqlInsert>parse("insert" +
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
                "(endpt: $v9, endpt: $v10) isa edge; "));
        tx.commit();

        GraknGraphProperties graphProperties = new GraknGraphProperties("localhost:48555", keyspaceName);

        double computedTransitivity = GlobalTransitivity.computeTransitivity(graphProperties);
        double correctTransitivity = 0.25;
        double allowedDeviationFraction = 0.0000001;
        client.keyspaces().delete(keyspaceName);
        session.close();
        assertEquals(correctTransitivity, computedTransitivity, allowedDeviationFraction * correctTransitivity);
    }

}
