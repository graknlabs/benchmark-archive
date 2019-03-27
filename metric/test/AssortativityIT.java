/*
 *  GRAKN.AI - THE KNOWLEDGE GRAPH
 *  Copyright (C) 2019 Grakn Labs Ltd
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package grakn.benchmark.metric.test;

import grakn.benchmark.metric.Assortativity;
import grakn.benchmark.metric.GraknGraphProperties;
import grakn.benchmark.metric.StandardGraphProperties;
import grakn.client.GraknClient;
import grakn.core.concept.answer.ConceptMap;
import graql.lang.Graql;
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
        GraknClient client = new GraknClient("localhost:48555");

        // define basic schema
        String keyspaceName = "assortativity_it";
        client.keyspaces().delete(keyspaceName);
        GraknClient.Session session = client.session(keyspaceName);
        GraknClient.Transaction tx = session.transaction().write();
        List<ConceptMap> answer = tx.execute(Graql.parse("define vertex sub entity, plays endpt; edge sub relation, relates endpt;").asDefine());
        tx.commit();

        // insert same data as `binaryGraph.csv`
        tx = session.transaction().write();
        answer = tx.execute(Graql.parse("insert" +
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
                "(endpt: $v9, endpt: $v10) isa edge; ").asInsert());
        tx.commit();

        GraknGraphProperties graphProperties = new GraknGraphProperties("localhost:48555", keyspaceName);

        double computedAssortativity = Assortativity.computeAssortativity(Assortativity.jointDegreeOccurrence(graphProperties));
        double correctAssortativity = -0.38888888888888995;
        double allowedDeviation = 0.000001;
        client.keyspaces().delete(keyspaceName);
        session.close();
        assertEquals(correctAssortativity, computedAssortativity, allowedDeviation);
    }


    @Test
    public void graknUnaryBinaryGraphAssortativity() {
        GraknClient client = new GraknClient("localhost:48555");

        // define basic schema
        String keyspaceName = "assortativity_it";
        client.keyspaces().delete(keyspaceName);
        GraknClient.Session session = client.session(keyspaceName);
        GraknClient.Transaction tx = session.transaction().write();
        List<?> answer = tx.execute(Graql.parse("define vertex sub entity, plays endpt; edge sub relation, relates endpt;").asDefine());
        tx.commit();

        // insert same data as `unaryBinaryGraph.csv`
        tx = session.transaction().write();
        answer = tx.execute(Graql.parse("insert" +
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
                "(endpt: $v9, endpt: $v10) isa edge; ").asInsert());
        tx.commit();

        GraknGraphProperties graphProperties = new GraknGraphProperties("localhost:48555", keyspaceName);

        double computedAssortativity = Assortativity.computeAssortativity(Assortativity.jointDegreeOccurrence(graphProperties));
        double correctAssortativity = -0.2767857142857146;
        double allowedDeviation = 0.000001;
        client.keyspaces().delete(keyspaceName);
        session.close();
        assertEquals(correctAssortativity, computedAssortativity, allowedDeviation);
    }
}
