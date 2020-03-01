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

package grakn.benchmark.generator.provider.value;

import java.util.Random;

/**


/**
 * Generate doubles from a slightly bigger gaussian distribution each time sampled
 */
public class ScalingGaussianDoubleProvider implements ValueProvider<Double> {

    int timesQueried = 0;
    double scalingFactor;
    Random random;

    public ScalingGaussianDoubleProvider(double scalingFactor) {
        random = new Random(0);
        this.scalingFactor = scalingFactor;
    }

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public Double next() {
        timesQueried++;
        double normalValue = this.random.nextGaussian();
        double scaled = normalValue * timesQueried * scalingFactor;
        return scaled;
    }
}
