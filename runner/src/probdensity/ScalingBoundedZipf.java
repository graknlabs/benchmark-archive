package grakn.benchmark.runner.probdensity;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.solvers.BrentSolver;
import org.apache.commons.math3.distribution.ZipfDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.RandomGeneratorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.function.Supplier;

public class ScalingBoundedZipf implements ProbabilityDensityFunction {

    private static final Logger LOG = LoggerFactory.getLogger(ScalingBoundedZipf.class);

    private Random rand;
    RandomGenerator randomGenerator;

    private double rangeLimitFraction;

    private int previousScale;
    private double previousExponent;
    private ZipfDistribution zipf;

    private Supplier<Integer> scaleSupplier;

    /**
     *
     * @param random
     * @param scaleSupplier
     * @param rangeLimitFactor -- fraction of scale supplied by scaleSupplier.get() to use as the upper bound of the Zipf dist
     * @param startingExponentForScale40 -- Greater than 1.0: this parameter tells us what the exponent for the zipf dist would be, if the scale supplied by scaleSupplier.get() is 40
     */
    public ScalingBoundedZipf(Random random, Supplier<Integer> scaleSupplier, double rangeLimitFactor, double startingExponentForScale40) {

        if (startingExponentForScale40 <= 1.0) {
            throw new RuntimeException("Require starting expontent for zipf to be > 1.0, is: " + startingExponentForScale40);
        }

        this.rand = random;
        this.rangeLimitFraction = rangeLimitFactor;

        int dummyStartingScale = 40;
        this.previousScale = dummyStartingScale;
        this.previousExponent = startingExponentForScale40;

        this.scaleSupplier = scaleSupplier;

        // convert random to Apache Math3 RandomGenerator
        randomGenerator = RandomGeneratorFactory.createRandomGenerator(this.rand);
        // initialize zipf
        int startingRange = (int)(this.previousScale* this.rangeLimitFraction);
        this.zipf = new ZipfDistribution(randomGenerator, startingRange, this.previousExponent);

        LOG.debug("Initialized dummy zipf distribution with limit: " + this.previousScale +
                ", exponent: " + startingExponentForScale40 +
                ", which therefore has mean: " + getNumericalMean());
    }

    public double getNumericalMean() {
        return this.zipf.getNumericalMean();
    }

    @Override
    public int sample() {

        int newScale = this.scaleSupplier.get();

        if (newScale != previousScale && newScale != 0) {

            double expLowerBound = 1.0;
            double expUpperBound = 100.0;

            NewExponentFinder func = new NewExponentFinder(previousScale, newScale, zipf);
            double newExponent;
            int newRange = (int) (newScale * this.rangeLimitFraction);

            // we can't produce means less than 1.0
            // if this condition is true, we are searching for an exponent that produces
            // a mean less than 1.0
            // in this case, skip (could also just use some upper bound
            if (func.value(expLowerBound) <= 0 && func.value(expUpperBound) <= 0) {
//                newExponent = Double.MAX_VALUE;
                return 1;
            } else if (func.value(expLowerBound) > 0 && func.value(expUpperBound) > 0) {
//                newExponent = previousExponent;
                throw new RuntimeException("Shouldn't be here...");
            } else {
                // updated scale means we need to update our zipf distribution
                BrentSolver solver = new BrentSolver();
                newExponent = solver.solve(100, func, expLowerBound, expUpperBound, previousExponent);
            }
            previousScale = newScale;
            previousExponent = newExponent;
            this.zipf = new ZipfDistribution(randomGenerator, previousScale, previousExponent);
        } else if (newScale == 0) {
            // just return 0 if the allowed range is 0 length
            return 0;
        }
        return this.zipf.sample();
    }


    private static class NewExponentFinder implements UnivariateFunction {
        private int previousScale;
        private int newScale;
        private double previousMean;

        public NewExponentFinder(int previousScale, int newScale, ZipfDistribution previousZipfDistribution) {
            this.previousScale = previousScale;
            this.newScale = newScale;
            this.previousMean = previousZipfDistribution.getNumericalMean();
        }

        public double value(double exponent) {
            ZipfDistribution newZipfDistribution = new ZipfDistribution(newScale, exponent);
            double newMean = newZipfDistribution.getNumericalMean();
            return (previousMean/previousScale) - (newMean/newScale);
        }
    }
}
