package grakn.benchmark.profiler.generator.query;

import grakn.benchmark.profiler.generator.probdensity.FixedConstant;
import grakn.benchmark.profiler.generator.provider.value.RandomStringProvider;
import grakn.benchmark.profiler.generator.provider.value.UniqueIntegerProvider;
import grakn.benchmark.profiler.generator.strategy.AttributeStrategy;
import grakn.core.graql.InsertQuery;
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

        Iterator<InsertQuery> insertAttributeQueries = insertAttributeQueryGenerator.generate();

        int nextValue = 0;
        while (insertAttributeQueries.hasNext()) {
            InsertQuery query = insertAttributeQueries.next();
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

        Iterator<InsertQuery> insertAttributeQueries = insertAttributeQueryGenerator.generate();

        for (int i = 0; i < 3; i++) {
            assertTrue(insertAttributeQueries.hasNext());
            insertAttributeQueries.next();
        }
        assertFalse(insertAttributeQueries.hasNext());
    }


}
