package grakn.benchmark.metric;

import org.apache.commons.math3.util.Pair;

import java.util.Set;
import java.util.stream.Stream;

public class GraknGraphProperties implements GraphProperties {

    public GraknGraphProperties(String uri, String keyspace) {

    }


    @Override
    public long maxDegree() {
        return 0;
    }

    @Override
    public Stream<Pair<Set<String>, Set<String>>> connectedEdgePairs(boolean requireAtLeastThreeUniqueVertices) {
        return null;
    }

    @Override
    public Stream<Pair<Integer, Integer>> connectedVertexDegrees() {
        return null;
    }

    @Override
    public Stream<Long> vertexDegree() {
        return null;
    }

    @Override
    public Set<String> neighbors(String vertexId) {
        return null;
    }
}
