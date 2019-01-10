/*
 *  GRAKN.AI - THE KNOWLEDGE GRAPH
 *  Copyright (C) 2018 Grakn Labs Ltd
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

package grakn.benchmark.runner.probdensity;



import java.util.Random;

import static java.lang.Integer.max;

/**
 *
 */
public class FixedDiscreteGaussian implements ProbabilityDensityFunction {
    private Random rand;
    private double mean;
    private double stddev;

    /**
     * @param rand
     * @param mean
     * @param stddev
     */
    public FixedDiscreteGaussian(Random rand, double mean, double stddev) {
        this.rand = rand;
        this.mean = mean;
        this.stddev = stddev;
    }

    /**
     * @return
     */
    public int sample() {
        double z = rand.nextGaussian();
        return max(0, (int) (stddev * z + mean));
    }
}