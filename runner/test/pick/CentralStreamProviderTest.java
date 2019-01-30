package grakn.benchmark.runner.pick;

import grakn.benchmark.runner.probdensity.FixedConstant;
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

        StreamInterface<Integer> streamer = mock(StreamInterface.class);
        List<Integer> ints = IntStream.range(0, 10).boxed().collect(Collectors.toList());
        when(streamer.getStream(any())).thenReturn(ints.stream());

        FixedConstant five = new FixedConstant(5);

        CentralStreamProvider<Integer> centralStreamProvider = new CentralStreamProvider<>(one, streamer);
        Stream<Integer> centralStream = centralStreamProvider.getStream(five, null);

        List<Integer> centralIntegers = centralStream.collect(Collectors.toList());

        List<Integer> expectedIntegers = Arrays.asList(0, 0, 0, 0, 0);

        Assert.assertThat(centralIntegers, is(expectedIntegers));
    }

    @Test
    public void whenMultipleCentralObject_objectsCyclicallyRepeated() {

        FixedConstant three = new FixedConstant(3);

        StreamInterface<Integer> streamer = mock(StreamInterface.class);
        List<Integer> ints = IntStream.range(0, 10).boxed().collect(Collectors.toList());
        when(streamer.getStream(any())).thenReturn(ints.stream());

        FixedConstant five = new FixedConstant(5);

        CentralStreamProvider<Integer> centralStreamProvider = new CentralStreamProvider<>(three, streamer);
        Stream<Integer> centralStream = centralStreamProvider.getStream(five, null);

        List<Integer> centralIntegers = centralStream.collect(Collectors.toList());

        List<Integer> expectedIntegers = Arrays.asList(0, 1, 2, 0, 1);

        Assert.assertThat(centralIntegers, is(expectedIntegers));
    }

    @Test
    public void whenMultipleCentralObjectCalledTwice_objectsCycleContinued() {

        FixedConstant three = new FixedConstant(3);

        StreamInterface<Integer> streamer = mock(StreamInterface.class);
        List<Integer> ints = IntStream.range(0, 10).boxed().collect(Collectors.toList());
        when(streamer.getStream(any())).thenReturn(ints.stream());

        FixedConstant five = new FixedConstant(5);

        CentralStreamProvider<Integer> centralStreamProvider = new CentralStreamProvider<>(three, streamer);
        Stream<Integer> centralStream = centralStreamProvider.getStream(five, null);

        List<Integer> centralIntegers = centralStream.collect(Collectors.toList());
        List<Integer> expectedIntegers = Arrays.asList(0, 1, 2, 0, 1);
        Assert.assertThat(centralIntegers, is(expectedIntegers));

        Stream<Integer> centralStreamTwo = centralStreamProvider.getStream(five, null);
        List<Integer> centralIntegersTwo = centralStreamTwo.collect(Collectors.toList());
        List<Integer> expectedIntegersTwo = Arrays.asList(2, 0, 1, 2, 0);
        Assert.assertThat(centralIntegersTwo, is(expectedIntegersTwo));
    }


    @Test
    public void whenMultipleCentralObjectCalledTwiceWithReset_cycleIsReset() {

        FixedConstant three = new FixedConstant(3);

        StreamInterface<Integer> streamer = mock(StreamInterface.class);
        List<Integer> ints = IntStream.range(0, 10).boxed().collect(Collectors.toList());
        when(streamer.getStream(any())).thenReturn(ints.stream()).thenReturn(ints.stream());

        FixedConstant five = new FixedConstant(5);

        CentralStreamProvider<Integer> centralStreamProvider = new CentralStreamProvider<>(three, streamer);
        Stream<Integer> centralStream = centralStreamProvider.getStream(five, null);

        List<Integer> centralIntegers = centralStream.collect(Collectors.toList());
        List<Integer> expectedIntegers = Arrays.asList(0, 1, 2, 0, 1);
        Assert.assertThat(centralIntegers, is(expectedIntegers));

        centralStreamProvider.reset();

        Stream<Integer> centralStreamTwo = centralStreamProvider.getStream(five, null);
        List<Integer> centralIntegersTwo = centralStreamTwo.collect(Collectors.toList());
        List<Integer> expectedIntegersTwo = Arrays.asList(0, 1, 2, 0, 1);
        Assert.assertThat(centralIntegersTwo, is(expectedIntegersTwo));
    }
}
