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
