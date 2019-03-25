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

package grakn.benchmark.generator.util;

import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

/**
 * Provides functionality to randomly choose from a weighted set of elements.
 *
 * @param <T> Type of elements in collection
 */
public class WeightedPicker<T> {
    private final NavigableMap<Double, T> map = new TreeMap<>();
    private final Random random;
    private double total = 0;

    public WeightedPicker(Random random) {
        this.random = random;
    }

    public WeightedPicker<T> add(double weight, T element) {
        if (weight <= 0) throw new IllegalArgumentException("Weight must be greater than zero.");
        total += weight;
        map.put(total, element);
        return this;
    }

    public T sample() {
        double value = random.nextDouble() * total;
        return map.higherEntry(value).getValue();
    }
}
