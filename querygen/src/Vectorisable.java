package grakn.benchmark.querygen;

import java.util.List;

public interface Vectorisable {
    public abstract List<Double> asVector();
}
