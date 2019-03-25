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

import grakn.benchmark.generator.probdensity.FixedConstant;
import grakn.benchmark.generator.provider.value.RandomStringProvider;
import grakn.benchmark.generator.provider.value.UniqueIntegerProvider;
import grakn.benchmark.generator.strategy.AttributeStrategy;
import graql.lang.query.GraqlInsert;
import org.junit.Test;

import java.util.Iterator;
import java.util.Random;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AttributeGeneratorTest {

    @Test
    public void whenInsertingIntegers_thenIteratorContainsCorrectValues() {
        AttributeStrategy<Integer> strategy = mock(AttributeStrategy.class);
        Iterator<Integer> intProvider = new UniqueIntegerProvider(0);
        when(strategy.getValueProvider()).thenReturn(intProvider);
        when(strategy.getTypeLabel()).thenReturn("age");
        when(strategy.getNumInstancesPDF()).thenReturn(new FixedConstant(3)); // always generate 3

        AttributeGenerator<Integer> insertAttributeQueryGenerator = new AttributeGenerator<>(strategy);

        Iterator<GraqlInsert> insertAttributeQueries = insertAttributeQueryGenerator.generate();

        int nextValue = 0;
        while (insertAttributeQueries.hasNext()) {
            GraqlInsert query = insertAttributeQueries.next();
            String queryString = query.toString();

            assertTrue(queryString.startsWith("insert"));
            assertTrue(queryString.contains(" " + Integer.toString(nextValue) + ";"));
            assertTrue(queryString.contains("isa age"));
            nextValue++;
        }
    }

    @Test
    public void whenInsertingThreeStrings_thenIteratorStopsAfterCorrectNumber() {
        AttributeStrategy<String> strategy = mock(AttributeStrategy.class);
        Iterator<String> stringProvider = new RandomStringProvider(new Random(), 5);
        when(strategy.getValueProvider()).thenReturn(stringProvider);
        when(strategy.getTypeLabel()).thenReturn("name");
        when(strategy.getNumInstancesPDF()).thenReturn(new FixedConstant(3)); // always generate 3 insert queries

        AttributeGenerator<String> insertAttributeQueryGenerator = new AttributeGenerator<>(strategy);

        Iterator<GraqlInsert> insertAttributeQueries = insertAttributeQueryGenerator.generate();

        for (int i = 0; i < 3; i++) {
            assertTrue(insertAttributeQueries.hasNext());
            insertAttributeQueries.next();
        }
        assertFalse(insertAttributeQueries.hasNext());
    }


}
