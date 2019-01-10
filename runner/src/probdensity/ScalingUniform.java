package grakn.benchmark.runner.probdensity;

import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 *
 */
public class ScalingUniform implements ProbabilityDensityFunction {

    private Random rand;
    private Supplier<Integer> scaleSupplier;
    private double lowerBoundFactor;
    private double upperBoundFactor;

    public ScalingUniform(Random rand, Supplier<Integer> scaleSupplier, double lowerBoundFactor, double upperBoundFactor) {
        this.rand = rand;
        this.scaleSupplier = scaleSupplier;
        this.lowerBoundFactor = lowerBoundFactor;
        this.upperBoundFactor = upperBoundFactor;
    }

    /**
     * @return
     */
    @Override
    public int sample() {
        Integer scale = scaleSupplier.get();
        int lowerBound = (int)(scale * this.lowerBoundFactor);
        int upperBound = (int)(scale * this.upperBoundFactor);
        IntStream intStream = rand.ints(1, lowerBound, upperBound + 1);
        return intStream.findFirst().getAsInt();
    }
}
