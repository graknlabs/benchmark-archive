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

import grakn.benchmark.generator.probdensity.FixedConstant;
import grakn.benchmark.generator.provider.key.CountingKeyProvider;
import grakn.benchmark.generator.strategy.EntityStrategy;
import graql.lang.query.GraqlInsert;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EntityGeneratorTest {

    @Test
    public void whenPdfZero_generateNoInsertQueries() {
        EntityStrategy strategy = mock(EntityStrategy.class);
        when(strategy.getTypeLabel()).thenReturn("person");
        when(strategy.getNumInstancesPDF()).thenReturn(new FixedConstant(0)); // always generate 3
        when(strategy.getConceptKeyProvider()).thenReturn(new CountingKeyProvider(0));

        EntityGenerator generator = new EntityGenerator(strategy);
        Iterator<GraqlInsert> insertEntityQueries = generator.generate();
        assertFalse(insertEntityQueries.hasNext());
    }

    @Test
    public void whenPdfConstantFive_generateFiveInsertQueries() {
        EntityStrategy strategy = mock(EntityStrategy.class);
        when(strategy.getTypeLabel()).thenReturn("person");
        when(strategy.getNumInstancesPDF()).thenReturn(new FixedConstant(5)); // always generate 3
        when(strategy.getConceptKeyProvider()).thenReturn(new CountingKeyProvider(0));

        EntityGenerator generator = new EntityGenerator(strategy);
        Iterator<GraqlInsert> insertEntityQueries = generator.generate();

        for (int i = 0; i < 5; i++) {
            assertTrue(insertEntityQueries.hasNext());
            insertEntityQueries.next();
        }
        assertFalse(insertEntityQueries.hasNext());
    }
}
