package grakn.benchmark.profiler.generator.query;

import grakn.benchmark.profiler.generator.probdensity.FixedConstant;
import grakn.benchmark.profiler.generator.strategy.EntityStrategy;
import grakn.core.graql.query.query.GraqlInsert;
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

        EntityGenerator generator = new EntityGenerator(strategy);
        Iterator<GraqlInsert> insertEntityQueries = generator.generate();
        assertFalse(insertEntityQueries.hasNext());
    }

    @Test
    public void whenPdfConstantFive_generateFiveInsertQueries() {
        EntityStrategy strategy = mock(EntityStrategy.class);
        when(strategy.getTypeLabel()).thenReturn("person");
        when(strategy.getNumInstancesPDF()).thenReturn(new FixedConstant(5)); // always generate 3

        EntityGenerator generator = new EntityGenerator(strategy);
        Iterator<GraqlInsert> insertEntityQueries = generator.generate();

        for (int i = 0; i < 5; i++) {
            assertTrue(insertEntityQueries.hasNext());
            insertEntityQueries.next();
        }
        assertFalse(insertEntityQueries.hasNext());
    }
}
