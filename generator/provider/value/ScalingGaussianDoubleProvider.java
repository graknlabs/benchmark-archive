package grakn.benchmark.generator.provider.value;

import java.util.Random;

/**


/**
 * Generate doubles from a slightly bigger gaussian distribution each time sampled
 */
public class ScalingGaussianDoubleProvider implements ValueProvider<Double> {

    int timesQueried = 0;
    double scalingFactor;
    Random random;

    public ScalingGaussianDoubleProvider(double scalingFactor) {
        random = new Random(0);
        this.scalingFactor = scalingFactor;
    }

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public Double next() {
        timesQueried++;
        double normalValue = this.random.nextGaussian();
        double scaled = normalValue * timesQueried * scalingFactor;
        return scaled;
    }
}
