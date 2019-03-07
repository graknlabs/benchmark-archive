package grakn.benchmark.profiler.generator.probdensity;

import java.util.Random;
import java.util.function.Supplier;

import static java.lang.Integer.max;

/**
 *
 */
public class ScalingDiscreteGaussian implements ProbabilityDensityFunction {
    private Random rand;
    private Supplier<Integer> scaleSupplier;
    private double meanScaleFactor;
    private double stddevScaleFactor;

    private Integer next = null;
    private int lastScale = 0;

    /**
     */
    public ScalingDiscreteGaussian(Random rand, Supplier<Integer> scaleSupplier, double meanScaleFactor, double stddevScaleFactor) {
        this.rand = rand;
        this.scaleSupplier = scaleSupplier;
        this.meanScaleFactor = meanScaleFactor;
        this.stddevScaleFactor = stddevScaleFactor;
    }

    /**
     * @return
     */
    public int sample() {
        takeSampleIfNextNullOrScaleChanged();
        int val = next;
        next = null;
        return val;
    }


    /**
     * Peek returns the next sample
     * @return
     */
    @Override
    public int peek() {
        takeSampleIfNextNullOrScaleChanged();
        return next;
    }

    private void takeSampleIfNextNullOrScaleChanged() {
        int scale = scaleSupplier.get();
        if (next == null || scale != lastScale) {
            double z = rand.nextGaussian();
            double stddev = scale * stddevScaleFactor;
            double mean =  scale * meanScaleFactor;
            next = max(0, (int) (stddev * z + mean));
            lastScale = scale;
        }
    }
}
