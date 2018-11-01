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

package pick;


import ai.grakn.client.Grakn;
import java.util.Random;
import java.util.stream.Stream;

/**
 * @param <T>
 */
public abstract class Picker<T> implements PickerInterface<T> {
    protected Random rand;

    public Picker(Random rand) {
        this.rand = rand;
    }

    public Stream<Integer> getStreamOfRandomOffsets(Grakn.Transaction tx) {
        int typeCount = getConceptCount(tx);
        return RandomOffsetGenerator.generate(this.rand, typeCount);
    }

    public boolean checkAvailable(int requiredLength, Grakn.Transaction tx) {
        // If there aren't enough concepts to fulfill the number requested, then return false
        int typeCount = getConceptCount(tx);
        return (requiredLength <= typeCount);
    }

    protected abstract Integer getConceptCount(Grakn.Transaction tx);
}
