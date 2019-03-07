package grakn.benchmark.profiler.generator.provider.concept;

import grakn.benchmark.profiler.generator.probdensity.FixedConstant;
import grakn.core.concept.ConceptId;
import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CentralConceptProviderTest {

    @Test
    public void whenSingleCentralObject_objectIsRepeated() {
        FixedConstant one = new FixedConstant(1);
        Iterator<ConceptId> conceptIdProvider = IntStream.range(0, 10).mapToObj(i -> ConceptId.of(Integer.toString(i))).iterator();

        CentralConceptProvider centralConceptProvider = new CentralConceptProvider(one, conceptIdProvider);

        for (int i = 0; i < 5; i++) {
            assertEquals(ConceptId.of(Integer.toString(0)), centralConceptProvider.next());
        }
    }

    @Test
    public void whenMultipleCentralObject_objectsCyclicallyRepeated() {
        FixedConstant three = new FixedConstant(3);
        Iterator<ConceptId> conceptIdProvider = IntStream.range(0, 10).mapToObj(i -> ConceptId.of(Integer.toString(i))).iterator();
        CentralConceptProvider centralConceptProvider = new CentralConceptProvider(three, conceptIdProvider);

        List<ConceptId> expectedIds = Arrays.asList(0, 1, 2, 0, 1).stream().map(i -> ConceptId.of(Integer.toString(i))).collect(Collectors.toList());
        for (ConceptId expectedId : expectedIds) {
            assertEquals(expectedId, centralConceptProvider.next());
        }
    }

    @Test
    public void whenMultipleCentralObjectCalledTwice_objectsCycleContinued() {

        FixedConstant three = new FixedConstant(3);
        Iterator<ConceptId> conceptIdProvider = IntStream.range(0, 10).mapToObj(i -> ConceptId.of(Integer.toString(i))).iterator();
        CentralConceptProvider centralConceptProvider = new CentralConceptProvider(three, conceptIdProvider);

        List<ConceptId> expectedIds = Arrays.asList(0, 1, 2, 0, 1).stream().map(i -> ConceptId.of(Integer.toString(i))).collect(Collectors.toList());
        for (ConceptId expectedId : expectedIds) {
            assertEquals(expectedId, centralConceptProvider.next());
        }

        expectedIds = Arrays.asList(2, 0, 1, 2, 0).stream().map(i -> ConceptId.of(Integer.toString(i))).collect(Collectors.toList());
        for (ConceptId expectedId : expectedIds) {
            assertEquals(expectedId, centralConceptProvider.next());
        }
    }


    @Test
    public void whenMultipleCentralObjectCalledTwiceWithReset_cycleIsReset() {

        FixedConstant three = new FixedConstant(3);
        Iterator<ConceptId> conceptIdProvider = IntStream.range(0, 10).mapToObj(i -> ConceptId.of(Integer.toString(i))).iterator();
        CentralConceptProvider centralConceptProvider = new CentralConceptProvider(three, conceptIdProvider);

        List<ConceptId> expectedIds = Arrays.asList(0, 1, 2, 0, 1).stream().map(i -> ConceptId.of(Integer.toString(i))).collect(Collectors.toList());
        for (ConceptId expectedInteger : expectedIds) {
            assertEquals(expectedInteger, centralConceptProvider.next());
        }

        centralConceptProvider.resetUniqueness();

        List<ConceptId> expectedIdsAfterReset = Arrays.asList(3, 4, 5, 3, 4).stream().map(i -> ConceptId.of(Integer.toString(i))).collect(Collectors.toList());
        for (ConceptId expectedId : expectedIdsAfterReset) {
            assertEquals(expectedId, centralConceptProvider.next());
        }
    }

    @Test
    public void whenCentralObjectIsNotEmpty_hasNextNTrue() {
        FixedConstant three = new FixedConstant(3);
        Iterator<ConceptId> conceptIdProvider = IntStream.range(0, 10).mapToObj(i -> ConceptId.of(Integer.toString(i))).iterator();
        CentralConceptProvider centralConceptProvider = new CentralConceptProvider(three, conceptIdProvider);
        assertTrue(centralConceptProvider.hasNextN(1));
        assertTrue(centralConceptProvider.hasNextN(100));
    }

    @Test
    public void whenCentralObjectIsEmpty_hasNextNFalse() {
        FixedConstant three = new FixedConstant(3);
        Iterator<ConceptId> conceptIdProvider = Arrays.<ConceptId>asList().iterator();
        CentralConceptProvider centralConceptProvider = new CentralConceptProvider(three, conceptIdProvider);
        assertFalse(centralConceptProvider.hasNextN(1));
        assertFalse(centralConceptProvider.hasNextN(100));
    }
}
