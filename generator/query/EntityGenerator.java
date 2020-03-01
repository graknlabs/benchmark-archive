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

package grakn.benchmark.generator.query;

import grakn.benchmark.generator.strategy.EntityStrategy;
import graql.lang.Graql;
import graql.lang.query.GraqlInsert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

/**
 * Generates queries for inserting entity instances
 */
public class EntityGenerator implements QueryGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(EntityGenerator.class);

    private final EntityStrategy strategy;

    public EntityGenerator(EntityStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public Iterator<GraqlInsert> generate() {
        LOG.trace("Generating Entity Type " + strategy.getTypeLabel() + ", target quantity: " + strategy.getNumInstancesPDF().peek());

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
                return Graql.insert(Graql.var("x").isa(typeLabel).has("unique-key", strategy.getConceptKeyProvider().next()));
            }
        };
    }
}
