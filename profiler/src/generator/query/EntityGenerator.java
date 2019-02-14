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

package grakn.benchmark.profiler.generator.query;

import grakn.benchmark.profiler.generator.strategy.EntityStrategy;
import grakn.core.graql.query.Graql;
import grakn.core.graql.query.query.GraqlInsert;

import java.util.Iterator;

/**
 * Generates queries for inserting entity instances
 */
public class EntityGenerator implements QueryGenerator {
    private final EntityStrategy strategy;

    public EntityGenerator(EntityStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public Iterator<GraqlInsert> generate() {

        return new Iterator<GraqlInsert>() {
            String typeLabel = strategy.getTypeLabel();
            int queriesToGenerate = strategy.getNumInstancesPDF().sample();
            int queriesGenerated = 0;

            @Override
            public boolean hasNext() {
                return queriesGenerated < queriesToGenerate;
            }

            @Override
            public GraqlInsert next() {
                queriesGenerated++;
                return Graql.insert(Graql.var("x").isa(typeLabel));
            }
        };
    }
}