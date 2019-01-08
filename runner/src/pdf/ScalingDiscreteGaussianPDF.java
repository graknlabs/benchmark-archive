package grakn.benchmark.runner.pdf;

import java.util.Random;
import java.util.function.Supplier;

import static java.lang.Integer.max;

/**
 *
 */
public class ScalingDiscreteGaussianPDF implements InvariantPDF {
    private Random rand;
    private Supplier<Integer> scaleSupplier;
    private double meanScaleFraction;
    private double stddevScaleFraction;

    /**
     */
    public ScalingDiscreteGaussianPDF(Random rand, Supplier<Integer> scaleSupplier, double meanScaleFraction, double stddevScaleFraction) {
        this.rand = rand;
        this.scaleSupplier = scaleSupplier;
        this.meanScaleFraction = meanScaleFraction;
        this.stddevScaleFraction = stddevScaleFraction;
    }

    /**
     * @return
     */
    public int next() {
        double z = rand.nextGaussian();
        int scale = scaleSupplier.get();
        double stddev = scale * stddevScaleFraction;
        double mean =  scale * meanScaleFraction;
        return max(0, (int) (stddev * z + mean));
    }
}
