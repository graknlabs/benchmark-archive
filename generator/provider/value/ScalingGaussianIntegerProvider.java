package grakn.benchmark.generator.provider.value;

import java.util.Random;

/**
 * Generate integers from a slightly bigger gaussian distribution each time sampled
 * This will lead to lots of overlap between inserts (especially to start with)
 * this should stress attribute deduplicator somewhat (if implemented synchronously as part of commit especially)
 */
public class ScalingGaussianIntegerProvider implements ValueProvider<Integer> {

    int timesQueried = 0;
    double scalingFactor;
    Random random;

    public ScalingGaussianIntegerProvider(double scalingFactor) {
        random = new Random(0);
        this.scalingFactor = scalingFactor;
    }

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public Integer next() {
        timesQueried++;
        double normalValue = this.random.nextGaussian();
        double scaled = normalValue * timesQueried * scalingFactor;
        return (int)Math.floor(scaled);
    }
}
