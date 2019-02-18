package grakn.benchmark.profiler.generator.provider.concept;

import grakn.benchmark.profiler.generator.storage.ConceptStorage;
import grakn.core.graql.concept.ConceptId;
import org.junit.Test;

import java.util.Random;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConceptIdStorageProviderTest {

    @Test
    public void whenConceptCountZero_hasNextFalse() {
        ConceptStorage storage = mock(ConceptStorage.class);
        when(storage.getConceptCount("person")).thenReturn(0);
        Random random = null;
        ConceptIdStorageProvider conceptIdProvider = new ConceptIdStorageProvider(random, storage, "person");
        assertFalse(conceptIdProvider.hasNext());
    }

    @Test
    public void whenConceptCountNotZero_hasNextTrue() {
        ConceptStorage storage = mock(ConceptStorage.class);
        when(storage.getConceptCount("person")).thenReturn(1);
        Random random = null;
        ConceptIdStorageProvider conceptIdProvider = new ConceptIdStorageProvider(random, storage, "person");
        assertTrue(conceptIdProvider.hasNext());
    }

    @Test
    public void whenAskForNextId_returnCorrectId() {
        ConceptStorage storage = mock(ConceptStorage.class);
        when(storage.getConceptCount("person")).thenReturn(4);
        when(storage.getConceptId("person", 0)).thenReturn(ConceptId.of("a"));
        when(storage.getConceptId("person", 1)).thenReturn(ConceptId.of("b"));
        when(storage.getConceptId("person", 2)).thenReturn(ConceptId.of("c"));
        when(storage.getConceptId("person", 3)).thenReturn(ConceptId.of("d"));

        Random random = mock(Random.class);
        when(random.nextInt(4)).thenReturn(2).thenReturn(0).thenReturn(1).thenReturn(1);

        ConceptIdStorageProvider conceptIdProvider = new ConceptIdStorageProvider(random, storage, "person");

        assertEquals(ConceptId.of("c"), conceptIdProvider.next());
        assertEquals(ConceptId.of("a"), conceptIdProvider.next());
        assertEquals(ConceptId.of("b"), conceptIdProvider.next());
        assertEquals(ConceptId.of("b"), conceptIdProvider.next());

    }

    @Test
    public void whenCheckIfHasNextN_returnCorrectBoolean() {
        ConceptStorage storage = mock(ConceptStorage.class);
        when(storage.getConceptCount("person")).thenReturn(4);
        Random random = mock(Random.class);
        ConceptIdStorageProvider conceptIdProvider = new ConceptIdStorageProvider(random, storage, "person");

        assertTrue(conceptIdProvider.hasNextN(0));
        assertTrue(conceptIdProvider.hasNextN(1));
        assertTrue(conceptIdProvider.hasNextN(2));
        assertTrue(conceptIdProvider.hasNextN(3));
        assertTrue(conceptIdProvider.hasNextN(4));
        assertFalse(conceptIdProvider.hasNextN(5));
    }
}
