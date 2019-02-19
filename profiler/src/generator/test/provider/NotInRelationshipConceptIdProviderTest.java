package grakn.benchmark.profiler.generator.provider.concept;

import grakn.benchmark.profiler.generator.storage.ConceptStorage;
import grakn.core.graql.concept.ConceptId;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NotInRelationshipConceptIdProviderTest {

    @Test
    public void whenAllConceptsInRelationship_hasNextFalse() {
        ConceptStorage storage = mock(ConceptStorage.class);
        when(storage.getIdsNotPlayingRole("person", "friendship", "friend")).thenReturn(new LinkedList<>());
        Random random = null;
        NotInRelationshipConceptIdProvider conceptIdProvider = new NotInRelationshipConceptIdProvider(random, storage, "person", "friendship", "friend");
        assertFalse(conceptIdProvider.hasNext());
    }

    @Test
    public void whenNotAllConceptsInRelationship_hasNextTrue() {
        ConceptStorage storage = mock(ConceptStorage.class);
        when(storage.getIdsNotPlayingRole("person", "friendship", "friend")).thenReturn(Arrays.asList(ConceptId.of("a")));
        Random random = null;
        NotInRelationshipConceptIdProvider conceptIdProvider = new NotInRelationshipConceptIdProvider(random, storage, "person", "friendship", "friend");
        assertTrue(conceptIdProvider.hasNext());
    }

    @Test
    public void whenAskForNextId_returnCorrectId() {
        ConceptStorage storage = mock(ConceptStorage.class);
        when(storage.getConceptCount("person")).thenReturn(4);
        List<ConceptId> idsNotPlayingRole = Arrays.asList(ConceptId.of("a"), ConceptId.of("b"), ConceptId.of("c"), ConceptId.of("d"));
        when(storage.getIdsNotPlayingRole("person", "friendship", "friend")).thenReturn(idsNotPlayingRole);

        Random random = mock(Random.class);
        when(random.nextInt(4)).thenReturn(2).thenReturn(0).thenReturn(1).thenReturn(1);

        NotInRelationshipConceptIdProvider conceptIdProvider = new NotInRelationshipConceptIdProvider(random, storage, "person", "friendship", "friend");
        assertEquals(ConceptId.of("c"), conceptIdProvider.next());
        assertEquals(ConceptId.of("a"), conceptIdProvider.next());
        assertEquals(ConceptId.of("b"), conceptIdProvider.next());
        assertEquals(ConceptId.of("b"), conceptIdProvider.next());

    }

    @Test
    public void whenCheckIfHasNextN_returnCorrectBoolean() {
        ConceptStorage storage = mock(ConceptStorage.class);
        when(storage.getConceptCount("person")).thenReturn(4);
        List<ConceptId> idsNotPlayingRole = Arrays.asList(ConceptId.of("a"), ConceptId.of("b"), ConceptId.of("c"), ConceptId.of("d"));
        when(storage.getIdsNotPlayingRole("person", "friendship", "friend")).thenReturn(idsNotPlayingRole);

        Random random = mock(Random.class);
        NotInRelationshipConceptIdProvider conceptIdProvider = new NotInRelationshipConceptIdProvider(random, storage, "person", "friendship", "friend");

        assertTrue(conceptIdProvider.hasNextN(0));
        assertTrue(conceptIdProvider.hasNextN(1));
        assertTrue(conceptIdProvider.hasNextN(2));
        assertTrue(conceptIdProvider.hasNextN(3));
        assertTrue(conceptIdProvider.hasNextN(4));
        assertFalse(conceptIdProvider.hasNextN(5));
    }
}
