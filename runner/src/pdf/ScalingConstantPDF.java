package grakn.benchmark.runner.pdf;

import java.util.function.Supplier;

/**
 *
 */
public class ScalingConstantPDF implements InvariantPDF {

    private Supplier<Integer> scaleSupplier;
    private double scaleFraction;

    /**
     */
    public ScalingConstantPDF(Supplier<Integer> scaleSupplier, double scaleFraction) {
        this.scaleSupplier = scaleSupplier;
        this.scaleFraction = scaleFraction;
    }


    /**
     * @return
     */
    @Override
    public int next() {
        return (int)(scaleSupplier.get() * scaleFraction);
    }
}
