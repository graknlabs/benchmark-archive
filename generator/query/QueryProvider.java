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

package grakn.benchmark.generator.query;

import grakn.benchmark.generator.definition.DataGeneratorDefinition;
import grakn.benchmark.generator.strategy.AttributeStrategy;
import grakn.benchmark.generator.strategy.EntityStrategy;
import grakn.benchmark.generator.strategy.RelationStrategy;
import grakn.benchmark.generator.strategy.TypeStrategy;
import graql.lang.query.GraqlInsert;

import java.util.Iterator;

public class QueryProvider {
    private final DataGeneratorDefinition dataGeneratorDefinition;

    public QueryProvider(DataGeneratorDefinition dataGeneratorDefinition) {
        this.dataGeneratorDefinition = dataGeneratorDefinition;

    }

    public Iterator<GraqlInsert> nextQueryBatch() {
        QueryGenerator queryGenerator;
        TypeStrategy typeStrategy = dataGeneratorDefinition.sampleNextStrategy();


        if (typeStrategy instanceof EntityStrategy) {
            queryGenerator = new EntityGenerator((EntityStrategy) typeStrategy);
        } else if (typeStrategy instanceof RelationStrategy) {
            queryGenerator = new RelationGenerator((RelationStrategy) typeStrategy);
        } else if (typeStrategy instanceof AttributeStrategy) {
            queryGenerator = new AttributeGenerator((AttributeStrategy) typeStrategy);
        } else {
            throw new RuntimeException("Couldn't find a matching Generator for this strategy");
        }
        return queryGenerator.generate();
    }

}
