package grakn.benchmark.profiler.generator.probdensity;

import java.util.Random;
import java.util.function.Supplier;

/**
 *
 */
public class ScalingUniform implements ProbabilityDensityFunction {

    private Random rand;
    private Supplier<Integer> scaleSupplier;
    private double lowerBoundFactor;
    private double upperBoundFactor;

    private Integer next = null;
    private int lastScale = 0;

    public ScalingUniform(Random rand, Supplier<Integer> scaleSupplier, double lowerBoundFactor, double upperBoundFactor) {
        this.rand = rand;
        this.scaleSupplier = scaleSupplier;
        this.lowerBoundFactor = lowerBoundFactor;
        this.upperBoundFactor = upperBoundFactor;
    }

    @Override
    public int sample() {
        takeSampleIfNextNullOrScaleChanged();
        int val = next;
        next = null;
        return val;
    }

    @Override
    public int peek() {
        takeSampleIfNextNullOrScaleChanged();
        return next;
    }

    public void takeSampleIfNextNullOrScaleChanged() {
        int scale = scaleSupplier.get();
        if (next == null || lastScale != scale) {
            int lowerBound = (int) (scale * this.lowerBoundFactor);
            int upperBound = (int) (scale * this.upperBoundFactor);
            next = lowerBound + rand.nextInt(upperBound - lowerBound + 1);
        }
    }
}
