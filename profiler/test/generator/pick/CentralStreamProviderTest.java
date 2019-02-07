package grakn.benchmark.profiler.generator.pick;

import grakn.benchmark.profiler.generator.probdensity.FixedConstant;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CentralStreamProviderTest {

    @Test
    public void whenSingleCentralObject_objectIsRepeated() {

        FixedConstant one = new FixedConstant(1);

        Iterator<Integer> streamer = mock(Iterator.class);
        List<Integer> ints = IntStream.range(0, 10).boxed().collect(Collectors.toList());
        when(streamer.getIterator()).thenReturn(ints.stream());

        FixedConstant five = new FixedConstant(5);

        CentralStreamProvider<Integer> centralStreamProvider = new CentralStreamProvider<>(one, streamer);
        Stream<Integer> centralStream = centralStreamProvider.getStream(five);

        List<Integer> centralIntegers = centralStream.collect(Collectors.toList());

        List<Integer> expectedIntegers = Arrays.asList(0, 0, 0, 0, 0);

        Assert.assertThat(centralIntegers, is(expectedIntegers));
    }

    @Test
    public void whenMultipleCentralObject_objectsCyclicallyRepeated() {

        FixedConstant three = new FixedConstant(3);

        Iterator<Integer> streamer = mock(Iterator.class);
        List<Integer> ints = IntStream.range(0, 10).boxed().collect(Collectors.toList());
        when(streamer.getIterator()).thenReturn(ints.stream());

        FixedConstant five = new FixedConstant(5);

        CentralStreamProvider<Integer> centralStreamProvider = new CentralStreamProvider<>(three, streamer);
        Stream<Integer> centralStream = centralStreamProvider.getStream(five);

        List<Integer> centralIntegers = centralStream.collect(Collectors.toList());

        List<Integer> expectedIntegers = Arrays.asList(0, 1, 2, 0, 1);

        Assert.assertThat(centralIntegers, is(expectedIntegers));
    }

    @Test
    public void whenMultipleCentralObjectCalledTwice_objectsCycleContinued() {

        FixedConstant three = new FixedConstant(3);

        Iterator<Integer> streamer = mock(Iterator.class);
        List<Integer> ints = IntStream.range(0, 10).boxed().collect(Collectors.toList());
        when(streamer.getIterator()).thenReturn(ints.stream());

        FixedConstant five = new FixedConstant(5);

        CentralStreamProvider<Integer> centralStreamProvider = new CentralStreamProvider<>(three, streamer);
        Stream<Integer> centralStream = centralStreamProvider.getStream(five);

        List<Integer> centralIntegers = centralStream.collect(Collectors.toList());
        List<Integer> expectedIntegers = Arrays.asList(0, 1, 2, 0, 1);
        Assert.assertThat(centralIntegers, is(expectedIntegers));

        Stream<Integer> centralStreamTwo = centralStreamProvider.getStream(five);
        List<Integer> centralIntegersTwo = centralStreamTwo.collect(Collectors.toList());
        List<Integer> expectedIntegersTwo = Arrays.asList(2, 0, 1, 2, 0);
        Assert.assertThat(centralIntegersTwo, is(expectedIntegersTwo));
    }


    @Test
    public void whenMultipleCentralObjectCalledTwiceWithReset_cycleIsReset() {

        FixedConstant three = new FixedConstant(3);

        Iterator<Integer> streamer = mock(Iterator.class);
        List<Integer> ints = IntStream.range(0, 10).boxed().collect(Collectors.toList());
        when(streamer.getIterator()).thenReturn(ints.stream()).thenReturn(ints.stream());

        FixedConstant five = new FixedConstant(5);

        CentralStreamProvider<Integer> centralStreamProvider = new CentralStreamProvider<>(three, streamer);
        Stream<Integer> centralStream = centralStreamProvider.getStream(five);

        List<Integer> centralIntegers = centralStream.collect(Collectors.toList());
        List<Integer> expectedIntegers = Arrays.asList(0, 1, 2, 0, 1);
        Assert.assertThat(centralIntegers, is(expectedIntegers));

        centralStreamProvider.resetUniqueness();

        Stream<Integer> centralStreamTwo = centralStreamProvider.getStream(five);
        List<Integer> centralIntegersTwo = centralStreamTwo.collect(Collectors.toList());
        List<Integer> expectedIntegersTwo = Arrays.asList(0, 1, 2, 0, 1);
        Assert.assertThat(centralIntegersTwo, is(expectedIntegersTwo));
    }
}
