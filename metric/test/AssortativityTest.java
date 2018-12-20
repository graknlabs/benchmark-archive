package grakn.benchmark.metric.test;

import grakn.benchmark.metric.Assortativity;
import grakn.benchmark.metric.GraphProperties;
import org.apache.commons.math3.util.Pair;
import org.junit.Test;
import org.mockito.Mockito;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;


public class AssortativityTest {


    @Test
    public void fullyAssortatitiveJointDegreeToAssortativity() {

        // create identity matrix -- degrees only associate with themselves!
        INDArray jointDegreeMatrix = Nd4j.eye(4);
        INDArray normalizedJointDistribution = jointDegreeMatrix.div(jointDegreeMatrix.sumNumber());

        double computedAssortativity = Assortativity.computeAssortativity(normalizedJointDistribution);
        double correctAssortativity = 1.0;

        double allowedDeviationFraction = 0.0000001; // following numpy's "almost equal" being 7 sigfigs
        assertEquals(correctAssortativity, computedAssortativity, allowedDeviationFraction * correctAssortativity);
    }

    @Test
    public void dissortativeJointDegreeMatrixToAssortativity() {
        /*
        originating adjacency lists:
        adjacency = {
        1: [], # degree 1 connnected to degree 2
        2: [], # degree 1 connected to degree 2
        3: [1, 2],  # degree 2 connected to degree 1 twice
        4: [], # 1 -> 3
        5: [], # 1 -> 3
        6: [4, 5, 7], # 3 -> 1, 1, 2
        7: [], # 2 -> 3, 4
        8: [7, 9, 10], # 4 -> 2, 1, 1, 1
        9: [10], # 2 -> 4, 2
        10: [], # 1 -> 4
        }
        */

        INDArray jointDegreeMatrix = Nd4j.zeros(4,4);

        jointDegreeMatrix.putScalar(new long[] {1, 2}, 2);
        jointDegreeMatrix.putScalar(new long[] {2, 1}, 2);

        jointDegreeMatrix.putScalar(new long[] {1, 3}, 2);
        jointDegreeMatrix.putScalar(new long[] {3, 1}, 2);

        jointDegreeMatrix.putScalar(new long[] {3, 1}, 2);
        jointDegreeMatrix.putScalar(new long[] {3, 1}, 2);

        jointDegreeMatrix.putScalar(new long[] {2, 3}, 4);
        jointDegreeMatrix.putScalar(new long[] {3, 2}, 4);

        jointDegreeMatrix.putScalar(new long[] {2, 2}, 2);  // counterintuitive it's it's listed as 2 -- its only listed once!

        INDArray normalizedJointDistribution = jointDegreeMatrix.div(jointDegreeMatrix.sumNumber());

        double computedAssortativity = Assortativity.computeAssortativity(normalizedJointDistribution);
        double correctAssortativity = -0.431818181818182;

        double allowedDeviation = 0.000001;
        assertEquals(correctAssortativity, computedAssortativity, allowedDeviation);
        System.out.println(correctAssortativity);
        System.out.println(computedAssortativity);

    }


    @Test
    public void fullyAssortativeGraphPropertiesToJointDegreeMatrix() {
        GraphProperties mockProperties = Mockito.mock(GraphProperties.class);

        /*
        We stream the undirected edge's endpoint's degrees twice each - in each direction
        IE. an edge between a vertex of degree 1 and degree 2 produces two connected vertex degrees: (1,2) and (2,1)
         */

        List<Pair<Long, Long>> connectedVertexDegrees = Arrays.asList(
                new Pair<>(1l, 2l),
                new Pair<>(1l, 2l),
                new Pair<>(2l, 1l),
                new Pair<>(2l, 1l),

                new Pair<>(1l, 3l),
                new Pair<>(1l, 3l),
                new Pair<>(3l, 1l),
                new Pair<>(3l, 1l),

                new Pair<>(2l, 3l),
                new Pair<>(2l, 3l),
                new Pair<>(2l, 3l),
                new Pair<>(2l, 3l),
                new Pair<>(3l, 2l),
                new Pair<>(3l, 2l),
                new Pair<>(3l, 2l),
                new Pair<>(3l, 2l),

                new Pair<>(2l, 2l),
                new Pair<>(2l, 2l)
        );

        when(mockProperties.connectedVertexDegrees()).thenReturn(connectedVertexDegrees);
        when(mockProperties.maxDegree()).thenReturn(3l);

        INDArray jointDegreeMatrix = Assortativity.jointDegreeOccurrence(mockProperties);

        INDArray correctJointDegreeMatrix = Nd4j.zeros(4,4);

        correctJointDegreeMatrix.putScalar(new long[] {1, 2}, 2);
        correctJointDegreeMatrix.putScalar(new long[] {2, 1}, 2);

        correctJointDegreeMatrix.putScalar(new long[] {1, 3}, 2);
        correctJointDegreeMatrix.putScalar(new long[] {3, 1}, 2);

        correctJointDegreeMatrix.putScalar(new long[] {3, 1}, 2);
        correctJointDegreeMatrix.putScalar(new long[] {3, 1}, 2);

        correctJointDegreeMatrix.putScalar(new long[] {2, 3}, 4);
        correctJointDegreeMatrix.putScalar(new long[] {3, 2}, 4);

        correctJointDegreeMatrix.putScalar(new long[] {2, 2}, 2);  // counterintuitive it's it's listed as 2 -- its only listed once!
        correctJointDegreeMatrix = correctJointDegreeMatrix.div(correctJointDegreeMatrix.sumNumber());


        double allowedDelta = 0.000001;
        assertArrayEquals(correctJointDegreeMatrix.ravel().toDoubleVector(), jointDegreeMatrix.ravel().toDoubleVector(), allowedDelta);
        System.out.println(Arrays.toString(correctJointDegreeMatrix.ravel().toDoubleVector()));
        System.out.println(Arrays.toString(jointDegreeMatrix.ravel().toDoubleVector()));

    }

}
