/*
 *  GRAKN.AI - THE KNOWLEDGE GRAPH
 *  Copyright (C) 2019 Grakn Labs Ltd
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package grakn.benchmark.generator.probdensity;

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

    private Integer next = null;

    /**
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
        computeNextSample();
        int val = next;
        next = null;
        return val;
    }

    @Override
    public int peek() {
        computeNextSample();
        return next;
    }

    private void computeNextSample() {
        int newScale = scaleSupplier.get();
        if (next == null || newScale != previousScale) {
            if (newScale != previousScale && newScale != 0) {
                // this isn't a real zeta distribution, it's a zipf distribution
                // so we can apparently go down to exponents near 0, if the range isn't too large
                // in practical cases, we're probably looking at a range < 1 million nodes or so, which is fine
                double expLowerBound = 0.001;
                double expUpperBound = 100.0;

                int oldRange = (int) (this.previousScale * this.rangeLimitFraction);
                int newRange = (int) (newScale * this.rangeLimitFraction);
                NewExponentFinder func = new NewExponentFinder(oldRange, newRange, zipf);
                double newExponent;

                if (func.value(expLowerBound) <= 0 && func.value(expUpperBound) <= 0) {
                    // we can't produce means less than 1.0
                    // if this condition is true, we are searching for an exponent that produces
                    // a mean less than 1.0
                    // so just return the smallest value (= 1.0)
                    next = 1;
                } else if (func.value(expLowerBound) > 0 && func.value(expUpperBound) > 0) {
                    throw new RuntimeException("No solution for new Zipf distribution parameters");
                } else {
                    LOG.debug("Starting parameter search for new Zipf distribution exponent");
                    // updated scale means we need to update our zipf distribution
                    BrentSolver solver = new BrentSolver();
                    newExponent = solver.solve(100, func, expLowerBound, expUpperBound, previousExponent);
                    LOG.debug("Old (range, exponent) zipf parameters: (" + oldRange + ", " + previousExponent + "). New params: (" +
                            newRange + ", " + newExponent + ")");
                    zipf = new ZipfDistribution(randomGenerator, newRange, newExponent);
                    previousExponent = newExponent;
                    previousScale = newScale;
                    next = zipf.sample();
                }
            } else if (newScale == 0) {
                // just return 0 if the allowed range is 0 length
                next = 0;
            } else {
                // just sample from the current zipf again
                next = zipf.sample();
            }
        }
    }


    private static class NewExponentFinder implements UnivariateFunction {
        private int previousRange;
        private int newRange;
        private double previousMean;

        public NewExponentFinder(int previousRange, int newScale, ZipfDistribution previousZipfDistribution) {
            this.previousRange = previousRange;
            this.newRange = newScale;
            this.previousMean = previousZipfDistribution.getNumericalMean();
        }

        public double value(double exponent) {
            ZipfDistribution newZipfDistribution = new ZipfDistribution(newRange, exponent);
            double newMean = newZipfDistribution.getNumericalMean();
            return (previousMean/previousRange) - (newMean/newRange);
        }
    }
}
