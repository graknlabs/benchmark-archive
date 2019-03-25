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
