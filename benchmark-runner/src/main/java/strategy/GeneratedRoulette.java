package strategy;

import pdf.PDF;
import random.RandomValue;

import java.util.Random;

/**
 * Wraps RouletteWheel and populates it with provided value & weight providers
 * @param <T>
 */
public class GeneratedRoulette<T> implements PickableCollection<T> {

    private RouletteWheel<T> rouletteWheel;
    private Random rand;
    RandomValue<T> randomValueGenerator;
    PDF valueWeightPDF;

    /**
     *
     * @param random - Random instance
     * @param initialSize - number elements to generate
     * @param randomValueGenerator - provider for random values to populate
     * @param valueWeightPDF - provider for weights of random values
     */
    public GeneratedRoulette(Random random, int initialSize, RandomValue<T> randomValueGenerator, PDF valueWeightPDF) {
        this.rand = random;
        this.randomValueGenerator = randomValueGenerator;
        this.valueWeightPDF = valueWeightPDF;
        this.rouletteWheel = new RouletteWheel<T>(this.rand);

        growTo(initialSize);
    }

    public void growTo(int n) {
        for (int i = 0; i < n; i++) {
            T value = this.randomValueGenerator.next();
            double weight = this.valueWeightPDF.next();
            this.rouletteWheel.add(weight, value);
        }
    }

    public T next() {
        return this.rouletteWheel.next();
    }
}
