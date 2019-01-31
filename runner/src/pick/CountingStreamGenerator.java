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

package grakn.benchmark.runner.pick;

import grakn.core.client.Grakn;

import java.util.Random;
import java.util.stream.Stream;

/**
 *
 */
public class CountingStreamGenerator implements StreamInterface<Integer>{
    private int n;

    public CountingStreamGenerator(int start) {
        this.n = start - 1;
    }

    private int next() {
        this.n++;
        return this.n;
    }

    @Override
    public Stream<Integer> getStream(Grakn.Transaction tx) {
        return Stream.generate( () -> next());
    }

    @Override
    public boolean checkAvailable(int requiredLength, Grakn.Transaction tx) {
        return true;
    }
}
