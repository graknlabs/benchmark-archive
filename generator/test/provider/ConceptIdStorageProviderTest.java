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

package grakn.benchmark.generator.provider.concept;

import grakn.benchmark.generator.storage.ConceptStorage;
import grakn.core.concept.ConceptId;
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
