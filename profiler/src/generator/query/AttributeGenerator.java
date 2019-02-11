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

import grakn.benchmark.profiler.generator.strategy.AttributeStrategy;
import grakn.core.graql.Graql;
import grakn.core.graql.InsertQuery;
import grakn.core.graql.Var;

import java.util.Iterator;


/**
 * Generates queries for inserting attribute values
 */
public class AttributeGenerator<Datatype> implements QueryGenerator {
    private final AttributeStrategy<Datatype> strategy;

    public AttributeGenerator(AttributeStrategy<Datatype> strategy) {
        this.strategy = strategy;
    }

    @Override
    public Iterator<InsertQuery> generate() {
        return new Iterator<InsertQuery>() {
            String attributeTypeLabel = strategy.getTypeLabel();
            Iterator<Datatype> valueProvider = strategy.getValueProvider();
            int queriesToGenerate = strategy.getNumInstancesPDF().sample();
            int queriesGenerated = 0;

            @Override
            public boolean hasNext() {
                return (queriesGenerated < queriesToGenerate) && valueProvider.hasNext();
            }

            @Override
            public InsertQuery next() {
                queriesGenerated++;
                Var attr = Graql.var().asUserDefined();
                Datatype value = valueProvider.next(); // get one attribute value
                return Graql.insert(attr.isa(attributeTypeLabel), attr.val(value));
            }
        };
    }
}
