/*
 * Copyright (C) 2020 Grakn Labs
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package grakn.benchmark.generator.probdensity;



import java.util.Random;

import static java.lang.Integer.max;

/**
 *
 */
public class FixedDiscreteGaussian implements ProbabilityDensityFunction {
    private Random rand;
    private double mean;
    private double stddev;

    private Integer next;

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
    @Override
    public int sample() {
        takeSampleIfNextNull();
        int val = next;
        next = null;
        return val;
    }

    @Override
    public int peek() {
        takeSampleIfNextNull();
        return next;
    }

    private void takeSampleIfNextNull() {
        if (next == null) {
            double z = rand.nextGaussian();
            next = max(0, (int) (stddev * z + mean));
        }
    }
}
