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
import grakn.benchmark.generator.provider.value.ValueProvider;

import java.util.Iterator;


/**
 * A container for the three things required to define how to generate a new set of attributes:
 * - A attribute type label
 * - A PDF that can be sampled to indicate how big the new batch of attributes is going to be
 * - A value provider for the actual values of the attribute of this type
 */
public class AttributeStrategy<T> extends TypeStrategy {

    private final Iterator<T> valueProvider;

    public AttributeStrategy(String attributeTypeLabel,
                             ProbabilityDensityFunction numInstancesPDF,
                             ValueProvider<T> valueProvider) {
        super(attributeTypeLabel, numInstancesPDF);
        this.valueProvider = valueProvider;
    }

    public Iterator<T> getValueProvider() {
        return this.valueProvider;
    }
}
