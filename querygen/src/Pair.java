package grakn.benchmark.querygen;

import java.util.Objects;

public class Pair<S,T> {

    private S first;
    private T second;

    public Pair(S first, T second) {
        this.first = first;
        this.second = second;
    }

    public S getFirst() {
        return first;
    }

    public T getSecond() {
        return second;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof Pair) {
            Pair that = (Pair) o;
            return Objects.equals(this.first, that.first) &&
                    Objects.equals(this.second, that.second);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int h = 1;
        h *= 1000003;
        h ^= (first == null) ? 0 : first.hashCode();
        h *= 1000003;
        h ^= (second == null) ? 0 : second.hashCode();
        return h;
    }


}
