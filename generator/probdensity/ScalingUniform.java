/*
 *  GRAKN.AI - THE KNOWLEDGE GRAPH
 *  Copyright (C) 2019 GraknClient Labs Ltd
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

/**
 *
 */
public class ScalingUniform implements ProbabilityDensityFunction {

    private Random rand;
    private Supplier<Integer> scaleSupplier;
    private double lowerBoundFactor;
    private double upperBoundFactor;

    private Integer next = null;
    private int lastScale = 0;

    public ScalingUniform(Random rand, Supplier<Integer> scaleSupplier, double lowerBoundFactor, double upperBoundFactor) {
        this.rand = rand;
        this.scaleSupplier = scaleSupplier;
        this.lowerBoundFactor = lowerBoundFactor;
        this.upperBoundFactor = upperBoundFactor;
    }

    @Override
    public int sample() {
        takeSampleIfNextNullOrScaleChanged();
        int val = next;
        next = null;
        return val;
    }

    @Override
    public int peek() {
        takeSampleIfNextNullOrScaleChanged();
        return next;
    }

    public void takeSampleIfNextNullOrScaleChanged() {
        int scale = scaleSupplier.get();
        if (next == null || lastScale != scale) {
            int lowerBound = (int) (scale * this.lowerBoundFactor);
            int upperBound = (int) (scale * this.upperBoundFactor);
            next = lowerBound + rand.nextInt(upperBound - lowerBound + 1);
        }
    }
}
