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

import java.util.function.Supplier;

/**
 *
 */
public class ScalingConstant implements ProbabilityDensityFunction {

    private Supplier<Integer> scaleSupplier;
    private double scaleFactor;

    /**
     */
    public ScalingConstant(Supplier<Integer> scaleSupplier, double scaleFactor) {
        this.scaleSupplier = scaleSupplier;
        this.scaleFactor = scaleFactor;
    }


    /**
     * @return
     */
    @Override
    public int sample() {
        return (int)(scaleSupplier.get() * scaleFactor);
    }

    @Override
    public int peek() {
        return (int)(scaleSupplier.get() * scaleFactor);
    }
}
