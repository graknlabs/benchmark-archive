package grakn.benchmark.generator.provider.key;

public class CountingKeyProvider implements ConceptKeyProvider {
    private long n;

    public CountingKeyProvider(int start) {
        n = start - 1;
    }

    @Override
    public boolean hasNextN(int n) {
        return true;
    }

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public Long next() {
        n++;
        return n-1;
    }
}
