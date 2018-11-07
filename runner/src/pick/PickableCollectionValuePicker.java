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

package ai.grakn.benchmark.runner.pick;

import ai.grakn.client.Grakn;
import ai.grakn.benchmark.runner.strategy.PickableCollection;

import java.util.stream.Stream;

/**
 * @param <T>
 */
public class PickableCollectionValuePicker<T> implements StreamInterface<T> {

    private PickableCollection<T> valueOptions;

    public PickableCollectionValuePicker(PickableCollection<T> valueOptions) {
        this.valueOptions = valueOptions;
    }

    @Override
    public Stream<T> getStream(Grakn.Transaction tx) {
        return Stream.generate(() -> valueOptions.next());
    }

    // TODO could implement replacement/no replacement in PickableCollections if we want
    @Override
    public boolean checkAvailable(int requiredLength, Grakn.Transaction tx) {
        return true;
    }

}
