package grakn.benchmark.runner.pdf;

import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 *
 */
public class ScalingUniformPDF implements InvariantPDF {

    private Random rand;
    private Supplier<Integer> scaleSupplier;
    private double lowerBoundFraction;
    private double upperBoundFraction;

    public ScalingUniformPDF(Random rand, Supplier<Integer> scaleSupplier, double lowerBoundFraction, double upperBoundFraction) {
        this.rand = rand;
        this.scaleSupplier = scaleSupplier;
        this.lowerBoundFraction = lowerBoundFraction;
        this.upperBoundFraction = upperBoundFraction;
    }

    /**
     * @return
     */
    @Override
    public int next() {
        Integer scale = scaleSupplier.get();
        int lowerBound = (int)(scale * this.lowerBoundFraction);
        int upperBound = (int)(scale * this.upperBoundFraction);
        IntStream intStream = rand.ints(1, lowerBound, upperBound + 1);
        return intStream.findFirst().getAsInt();
    }
}
