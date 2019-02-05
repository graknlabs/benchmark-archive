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

package grakn.benchmark.profiler.generator.strategy;

import grakn.benchmark.profiler.generator.probdensity.ProbabilityDensityFunction;

/**
 */
public class TypeStrategy {
    private final String typeLabel;
    private final ProbabilityDensityFunction numInstancesPDF;

    public TypeStrategy(String typeLabel, ProbabilityDensityFunction numInstancesPDF){
        this.numInstancesPDF = numInstancesPDF;
        this.typeLabel = typeLabel;
    }

    public String getTypeLabel() {
        return typeLabel;
    }

    public ProbabilityDensityFunction getNumInstancesPDF() {
        return numInstancesPDF;
    }
}

