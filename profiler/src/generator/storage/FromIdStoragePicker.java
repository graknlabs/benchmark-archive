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

package grakn.benchmark.profiler.generator.storage;


import grakn.core.client.Grakn;
import grakn.benchmark.profiler.generator.pick.Picker;
import java.util.Random;

/**
 * Base class for the various different FromIdStoragePickers
 * This removes the need for generics in children
 * @param <T>
 */
public abstract class FromIdStoragePicker<T> extends Picker<T> {

    protected IdStore conceptStore;
    protected String typeLabel;

    public FromIdStoragePicker(Random rand, IdStore conceptStore, String typeLabel) {
        super(rand);
        this.conceptStore = conceptStore;
        this.typeLabel = typeLabel;
    }


    protected Integer getConceptCount(Grakn.Transaction tx) {
        return this.conceptStore.getConceptCount(this.typeLabel);
    }
}
