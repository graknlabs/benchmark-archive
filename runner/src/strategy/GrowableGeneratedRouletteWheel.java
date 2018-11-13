package grakn.benchmark.runner.strategy;

import grakn.benchmark.runner.pdf.PDF;
import grakn.benchmark.runner.pick.StreamInterface;

import java.util.Iterator;
import java.util.Random;

/**
 * RouletteWheel that populates with provided value & weight providers
 *
 * @param <T>
 */

public class GrowableGeneratedRouletteWheel<T> extends RouletteWheel<T> {

    PDF weightProviderPDF;
    Iterator<T> valueStream;

    /**
     *
     * @param random - Random instance
     */
    public GrowableGeneratedRouletteWheel(Random random, StreamInterface<T> valueStreamProvider, PDF weightProviderPDF) {
        super(random);
        this.weightProviderPDF = weightProviderPDF;
        this.valueStream = valueStreamProvider.getStream(null).iterator();
    }

    public void growTo(int n) {
        for (int i = 0; i < n; i++) {
            T value = this.valueStream.next();
            double weight = this.weightProviderPDF.next();
            add(weight, value);
        }
    }
}
