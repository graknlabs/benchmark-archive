package grakn.benchmark.runner.probdensity;

import java.util.function.Supplier;

/**
 *
 */
public class ScalingConstant implements ProbabilityDensityFunction {

    private Supplier<Integer> scaleSupplier;
    private double scaleFactor;

    /**
     */
    public ScalingConstant(Supplier<Integer> scaleSupplier, double scaleFactor) {
        this.scaleSupplier = scaleSupplier;
        this.scaleFactor = scaleFactor;
    }


    /**
     * @return
     */
    @Override
    public int sample() {
        return (int)(scaleSupplier.get() * scaleFactor);
    }
}
