package grakn.benchmark.runner.probdensity;

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
        double z = rand.nextGaussian();
        int scale = scaleSupplier.get();
        double stddev = scale * stddevScaleFactor;
        double mean =  scale * meanScaleFactor;
        return max(0, (int) (stddev * z + mean));
    }
}
