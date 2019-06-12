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

package grakn.benchmark.generator.provider.key;

import grakn.benchmark.generator.storage.ConceptStorage;
import org.junit.Test;

import java.util.Random;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConceptKeyStorageProviderTest {

    @Test
    public void whenConceptCountZero_hasNextFalse() {
        ConceptStorage storage = mock(ConceptStorage.class);
        when(storage.getConceptCount("person")).thenReturn(0);
        Random random = null;
        ConceptKeyStorageProvider conceptKeyProvider = new ConceptKeyStorageProvider(random, storage, "person");
        assertFalse(conceptKeyProvider.hasNext());
    }

    @Test
    public void whenConceptCountNotZero_hasNextTrue() {
        ConceptStorage storage = mock(ConceptStorage.class);
        when(storage.getConceptCount("person")).thenReturn(1);
        Random random = null;
        ConceptKeyStorageProvider conceptKeyProvider = new ConceptKeyStorageProvider(random, storage, "person");
        assertTrue(conceptKeyProvider.hasNext());
    }

    @Test
    public void whenAskForNextId_returnCorrectId() {
        ConceptStorage storage = mock(ConceptStorage.class);
        when(storage.getConceptCount("person")).thenReturn(4);
        when(storage.getConceptKey("person", 0)).thenReturn(1L);
        when(storage.getConceptKey("person", 1)).thenReturn(2L);
        when(storage.getConceptKey("person", 2)).thenReturn(3L);
        when(storage.getConceptKey("person", 3)).thenReturn(4L);

        Random random = mock(Random.class);
        when(random.nextInt(4)).thenReturn(2).thenReturn(0).thenReturn(1).thenReturn(1);

        ConceptKeyStorageProvider conceptKeyProvider = new ConceptKeyStorageProvider(random, storage, "person");

        assertEquals(3L, (long) conceptKeyProvider.next());
        assertEquals(1L, (long) conceptKeyProvider.next());
        assertEquals(2L, (long) conceptKeyProvider.next());
        assertEquals(2L, (long) conceptKeyProvider.next());

    }

    @Test
    public void whenCheckIfHasNextN_returnCorrectBoolean() {
        ConceptStorage storage = mock(ConceptStorage.class);
        when(storage.getConceptCount("person")).thenReturn(4);
        Random random = mock(Random.class);
        ConceptKeyStorageProvider conceptKeyProvider = new ConceptKeyStorageProvider(random, storage, "person");

        assertTrue(conceptKeyProvider.hasNextN(0));
        assertTrue(conceptKeyProvider.hasNextN(1));
        assertTrue(conceptKeyProvider.hasNextN(2));
        assertTrue(conceptKeyProvider.hasNextN(3));
        assertTrue(conceptKeyProvider.hasNextN(4));
        assertFalse(conceptKeyProvider.hasNextN(5));
    }
}
