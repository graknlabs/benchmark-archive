package grakn.benchmark.metric.test;

import grakn.benchmark.metric.DegreeDistribution;
import grakn.benchmark.metric.GraphProperties;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Mockito.when;

public class DegreeDistributionTest {

    @Test
    public void degreeStreamToPercentiles() {

        GraphProperties mockProperties = Mockito.mock(GraphProperties.class);

        long[] vertexDegrees = new long[] {
                0, 0, 0,
                1, 1,
                2, 2,
                3, 3,
                4,
                10
        };
        when(mockProperties.vertexDegree()).thenReturn(Arrays.stream(vertexDegrees).boxed().collect(Collectors.toList()));

        double[] percentiles = new double[] {0, 20, 50, 80, 100};
        long[] correctDegreeDistribution = new long[] {0, 0, 2, 3, 10};

        long[] computedDegreeDistribution = DegreeDistribution.discreteDistribution(mockProperties, percentiles);
        assertArrayEquals(correctDegreeDistribution, computedDegreeDistribution);
    }
}
