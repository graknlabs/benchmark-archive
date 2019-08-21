package grakn.benchmark.querygen.kmeans;

import java.util.List;

public interface Vectorisable {
    public abstract List<Double> asVector();
}
