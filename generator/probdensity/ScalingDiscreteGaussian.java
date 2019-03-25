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

package grakn.benchmark.generator.probdensity;

import java.util.Random;
import java.util.function.Supplier;

import static java.lang.Integer.max;

/**
 *
 */
public class ScalingDiscreteGaussian implements ProbabilityDensityFunction {
    private Random rand;
    private Supplier<Integer> scaleSupplier;
    private double meanScaleFactor;
    private double stddevScaleFactor;

    private Integer next = null;
    private int lastScale = 0;

    /**
     */
    public ScalingDiscreteGaussian(Random rand, Supplier<Integer> scaleSupplier, double meanScaleFactor, double stddevScaleFactor) {
        this.rand = rand;
        this.scaleSupplier = scaleSupplier;
        this.meanScaleFactor = meanScaleFactor;
        this.stddevScaleFactor = stddevScaleFactor;
    }

    /**
     * @return
     */
    public int sample() {
        takeSampleIfNextNullOrScaleChanged();
        int val = next;
        next = null;
        return val;
    }


    /**
     * Peek returns the next sample
     * @return
     */
    @Override
    public int peek() {
        takeSampleIfNextNullOrScaleChanged();
        return next;
    }

    private void takeSampleIfNextNullOrScaleChanged() {
        int scale = scaleSupplier.get();
        if (next == null || scale != lastScale) {
            double z = rand.nextGaussian();
            double stddev = scale * stddevScaleFactor;
            double mean =  scale * meanScaleFactor;
            next = max(0, (int) (stddev * z + mean));
            lastScale = scale;
        }
    }
}
