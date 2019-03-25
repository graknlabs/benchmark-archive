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

package grakn.benchmark.metric;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.BooleanIndexing;
import org.nd4j.linalg.indexing.conditions.Conditions;

import java.util.LinkedList;

public class DegreeDistribution {

    public static long[] discreteDistribution(GraphProperties properties, double[] percentiles) {


        // consume stream of Integers representing vertex degrees
        LinkedList<Long> allDegrees = new LinkedList<>();
        properties.vertexDegree().forEach(degree -> allDegrees.add(degree));
        // identity function unboxes Double to double
        double[] allDegreesArray = allDegrees.stream().mapToDouble(l -> l).toArray();

        INDArray degrees = Nd4j.create(allDegreesArray);

        // sort degrees increasing in order
        degrees = Nd4j.sort(degrees, true);


        long numDegrees = degrees.shape()[1]; // has shape (1, N)
        // convert the decimal percentiles into ND4J percentile array
        INDArray percentilesArray = Nd4j.create(percentiles);
        // convert further into indices of the degrees array
        INDArray percentileIndices = percentilesArray.mul(0.01 * numDegrees);
        // need to make sure none of the indices are == numDegrees (max element is maxDegrees - 1)
        clip(percentileIndices, 0, numDegrees - 1);
        // retrieve indicated indices
        INDArray degreeDistribution = degrees.get(percentileIndices);

        // return as long[]
        return degreeDistribution.toLongVector();

    }

    /**
     * Clip given ND4J array with min and max values IN PLACE
     * @param array
     * @param minValue
     * @param maxValue
     * @return
     */
    private static void clip(INDArray array, double minValue, double maxValue) {
        BooleanIndexing.replaceWhere(array, minValue, Conditions.lessThan(minValue));
        BooleanIndexing.replaceWhere(array, maxValue, Conditions.greaterThan(maxValue));
    }
}
