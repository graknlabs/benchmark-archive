package grakn.benchmark.profiler.generator.provider.value;

import grakn.benchmark.profiler.generator.probdensity.ProbabilityDensityFunction;

import static java.lang.Integer.max;

/**
 * An incrementing counter that repeats the current integer some number of times as given by the PDF
 */
public class RepeatingIntegerProvider implements ValueProvider<Integer> {

    private int n;
    private int repeats;
    private int repeatsRetrieved;
    private ProbabilityDensityFunction repetitionsGenerator;

    public RepeatingIntegerProvider(int start, ProbabilityDensityFunction repetitionsGenerator) {
        this.repetitionsGenerator = repetitionsGenerator;

        n = start - 1;
        repeats = max(1, repetitionsGenerator.sample());
        repeatsRetrieved = 0;
    }

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public Integer next() {
        if (repeatsRetrieved == repeats) {
            repeats = max(1, repetitionsGenerator.sample());
            repeatsRetrieved = 0;
            this.n++;
        }

        repeatsRetrieved++;
        return n;
    }
}
