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

/**
 *
 */
public class FixedConstant implements ProbabilityDensityFunction {

    private int constant;

    /**
     * @param constant
     */
    public FixedConstant(int constant) {
        this.constant = constant;
    }

    /**
     * @return
     */
    @Override
    public int sample() {
        return this.constant;
    }

    @Override
    public int peek() {
        return this.constant;
    }
}
