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

import grakn.benchmark.generator.probdensity.FixedConstant;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CentralConceptProviderTest {

    @Test
    public void whenSingleCentralObject_objectIsRepeated() {
        FixedConstant one = new FixedConstant(1);

        List<Long> conceptKeys = IntStream.range(0, 10).mapToLong(e->e).boxed().collect(Collectors.toList());
        ConceptKeyProvider conceptKeyProvider = mock(ConceptKeyProvider.class);
        when(conceptKeyProvider.hasNext()).thenReturn(true);
        when(conceptKeyProvider.hasNextN(1)).thenReturn(true);
        when(conceptKeyProvider.next()).thenReturn(conceptKeys.get(0), conceptKeys.get(1), conceptKeys.get(2), conceptKeys.get(3), conceptKeys.get(4));

        CentralConceptKeyProvider centralConceptKeyProvider = new CentralConceptKeyProvider(one, conceptKeyProvider);

        for (int i = 0; i < 5; i++) {
            assertEquals(0L, (long)centralConceptKeyProvider.next());
        }
    }

    @Test
    public void whenMultipleCentralObject_objectsCyclicallyRepeated() {
        FixedConstant three = new FixedConstant(3);

        List<Long> conceptKeys = IntStream.range(0, 10).mapToLong(e->e).boxed().collect(Collectors.toList());
        ConceptKeyProvider conceptKeyProvider = mock(ConceptKeyProvider.class);
        when(conceptKeyProvider.hasNext()).thenReturn(true);
        when(conceptKeyProvider.hasNextN(3)).thenReturn(true);
        when(conceptKeyProvider.next()).thenReturn(conceptKeys.get(0), conceptKeys.get(1), conceptKeys.get(2), conceptKeys.get(3), conceptKeys.get(4));


        CentralConceptKeyProvider centralConceptKeyProvider = new CentralConceptKeyProvider(three, conceptKeyProvider);

        List<Long> expectedKeys = Arrays.asList(0L, 1L, 2L, 0L, 1L);
        for (Long expectedKey : expectedKeys) {
            assertEquals(expectedKey, centralConceptKeyProvider.next());
        }
    }

    @Test
    public void whenMultipleCentralObjectCalledTwice_objectsCycleContinued() {

        FixedConstant three = new FixedConstant(3);

        List<Long> conceptKeys = IntStream.range(0, 10).mapToLong(e->e).boxed().collect(Collectors.toList());
        ConceptKeyProvider conceptKeyProvider = mock(ConceptKeyProvider.class);
        when(conceptKeyProvider.hasNext()).thenReturn(true);
        when(conceptKeyProvider.hasNextN(3)).thenReturn(true);
        when(conceptKeyProvider.next()).thenReturn(conceptKeys.get(0), conceptKeys.get(1), conceptKeys.get(2), conceptKeys.get(3), conceptKeys.get(4));

        CentralConceptKeyProvider centralConceptKeyProvider = new CentralConceptKeyProvider(three, conceptKeyProvider);

        List<Long> expectedKeys = Arrays.asList(0L, 1L, 2L, 0L, 1L);
        for (Long expectedKey : expectedKeys) {
            assertEquals(expectedKey, centralConceptKeyProvider.next());
        }

        expectedKeys = Arrays.asList(2L, 0L, 1L, 2L, 0L);
        for (Long expectedKey : expectedKeys) {
            assertEquals(expectedKey, centralConceptKeyProvider.next());
        }
    }


    @Test
    public void whenMultipleCentralObjectCalledTwiceWithReset_cycleIsReset() {

        FixedConstant three = new FixedConstant(3);

        List<Long> conceptKeys = IntStream.range(0, 10).mapToLong(e->e).boxed().collect(Collectors.toList());
        ConceptKeyProvider conceptKeyProvider = mock(ConceptKeyProvider.class);
        when(conceptKeyProvider.hasNext()).thenReturn(true);
        when(conceptKeyProvider.hasNextN(3)).thenReturn(true);
        when(conceptKeyProvider.next()).thenReturn(conceptKeys.get(0), conceptKeys.get(1), conceptKeys.get(2), conceptKeys.get(3), conceptKeys.get(4), conceptKeys.get(5));
        CentralConceptKeyProvider centralConceptKeyProvider = new CentralConceptKeyProvider(three, conceptKeyProvider);

        List<Long> expectedKeys = Arrays.asList(0L, 1L, 2L, 0L, 1L);
        for (Long expectedKey : expectedKeys) {
            assertEquals(expectedKey, centralConceptKeyProvider.next());
        }

        centralConceptKeyProvider.resetUniqueness();

        List<Long> expectedKeysAfterReset = Arrays.asList(3L, 4L, 5L, 3L, 4L);
        for (Long expectedKey : expectedKeysAfterReset) {
            assertEquals(expectedKey, centralConceptKeyProvider.next());
        }
    }

    @Test
    public void whenCentralObjectIsNotEmpty_hasNextNTrue() {
        FixedConstant three = new FixedConstant(3);
        List<Long> conceptKeys = IntStream.range(0, 10).mapToLong(e->e).boxed().collect(Collectors.toList());
        ConceptKeyProvider conceptKeyProvider = mock(ConceptKeyProvider.class);
        when(conceptKeyProvider.hasNext()).thenReturn(true);
        when(conceptKeyProvider.hasNextN(3)).thenReturn(true);
        when(conceptKeyProvider.next()).thenReturn(conceptKeys.get(0), conceptKeys.get(1), conceptKeys.get(2), conceptKeys.get(3), conceptKeys.get(4));
        CentralConceptKeyProvider centralConceptKeyProvider = new CentralConceptKeyProvider(three, conceptKeyProvider);
        assertTrue(centralConceptKeyProvider.hasNextN(1));
        assertTrue(centralConceptKeyProvider.hasNextN(100));
    }

    @Test
    public void whenCentralObjectIsEmpty_hasNextNFalse() {
        FixedConstant three = new FixedConstant(3);
        List<Long> conceptKeys = IntStream.range(0, 10).mapToLong(e->e).boxed().collect(Collectors.toList());
        ConceptKeyProvider conceptKeyProvider = mock(ConceptKeyProvider.class);
        when(conceptKeyProvider.hasNext()).thenReturn(false);
        when(conceptKeyProvider.hasNextN(3)).thenReturn(false);
        when(conceptKeyProvider.next()).thenReturn(conceptKeys.get(0), conceptKeys.get(1), conceptKeys.get(2), conceptKeys.get(3), conceptKeys.get(4));

        CentralConceptKeyProvider centralConceptKeyProvider = new CentralConceptKeyProvider(three, conceptKeyProvider);
        assertFalse(centralConceptKeyProvider.hasNextN(1));
        assertFalse(centralConceptKeyProvider.hasNextN(100));
    }
}
