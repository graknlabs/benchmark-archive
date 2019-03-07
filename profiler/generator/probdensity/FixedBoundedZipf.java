package grakn.benchmark.profiler.generator.probdensity;

import org.apache.commons.math3.distribution.ZipfDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.RandomGeneratorFactory;

import java.util.Random;

public class FixedBoundedZipf implements ProbabilityDensityFunction {
    private Random rand;
    private int rangeLimit;
    private double exponent;

    private ZipfDistribution zipf;

    private Integer next;

    public FixedBoundedZipf(Random random, int rangeLimit, double exponent) {
        this.rand = random;
        this.rangeLimit = rangeLimit;
        this.exponent = exponent;

        // convert random to Apache Math3 RandomGenerator
        RandomGenerator gen = RandomGeneratorFactory.createRandomGenerator(this.rand);
        // initialize zipf
        this.zipf = new ZipfDistribution(gen, this.rangeLimit, this.exponent);

        System.out.println("Initialized zipf distribution with numerical mean: " + getNumericalMean());
    }

    public double getNumericalMean() {
        return this.zipf.getNumericalMean();
    }

    @Override
    public int sample() {
        takeSampleIfNextNull();
        int val = next;
        next = null;
        return val;
    }

    @Override
    public int peek() {
        takeSampleIfNextNull();
        return next;
    }

    private void takeSampleIfNextNull() {
        if (next == null) {
            next = zipf.sample();
        }
    }

}
