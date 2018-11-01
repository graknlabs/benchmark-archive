package strategy;

import pdf.PDF;
import pick.StreamInterface;

import java.util.Random;
import java.util.stream.Stream;

/**
 * RouletteWheel that populates with provided value & weight providers
 *
 * @param <T>
 */

public class GrowableGeneratedRouletteWheel<T> extends RouletteWheel<T> {

    PDF weightProviderPDF;
    Stream<T> valueStream;

    /**
     *
     * @param random - Random instance
     */
    public GrowableGeneratedRouletteWheel(Random random, StreamInterface<T> valueStreamProvider, PDF weightProviderPDF) {
        super(random);
        this.weightProviderPDF = weightProviderPDF;
        this.valueStream = valueStreamProvider.getStream(null);
    }

    public void growTo(int n) {
        for (int i = 0; i < n; i++) {
            T value = this.valueStream.findFirst().get();
            double weight = this.weightProviderPDF.next();
            add(weight, value);
        }
    }
}
