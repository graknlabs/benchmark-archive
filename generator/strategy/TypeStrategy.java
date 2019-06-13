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

package grakn.benchmark.generator.strategy;

import grakn.benchmark.generator.probdensity.ProbabilityDensityFunction;
import grakn.benchmark.generator.provider.key.ConceptKeyProvider;

/**
 * A Type strategy is a container composed of:
 * - a type label defining what type to generate
 * - a PDF defining how many type instances to generate
 * and it is consumed by a generator.
 */
public abstract class TypeStrategy {
    private final String typeLabel;
    private final ProbabilityDensityFunction numInstancesPDF;
    final ConceptKeyProvider conceptKeyProvider;

    public TypeStrategy(String typeLabel, ProbabilityDensityFunction numInstancesPDF, ConceptKeyProvider conceptKeyProvider) {
        this.numInstancesPDF = numInstancesPDF;
        this.typeLabel = typeLabel;
        this.conceptKeyProvider = conceptKeyProvider;
    }

    public String getTypeLabel() {
        return typeLabel;
    }

    public ProbabilityDensityFunction getNumInstancesPDF() {
        return numInstancesPDF;
    }

    public ConceptKeyProvider getConceptKeyProvider() {
        return conceptKeyProvider;
    }
}

