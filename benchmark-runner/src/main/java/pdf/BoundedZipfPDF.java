package pdf;

import org.apache.commons.math3.distribution.ZipfDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.RandomGeneratorFactory;

import java.util.Random;

public class BoundedZipfPDF extends PDF {
    private Random rand;
    private int rangeLimit;
    private double exponent;

    private ZipfDistribution zipf;

    public BoundedZipfPDF(Random random, int rangeLimit, double exponent) {
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
    public int next() {
        return this.zipf.sample();
    }
}
