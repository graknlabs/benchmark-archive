package grakn.benchmark.metric;

import org.apache.commons.math3.util.Pair;

import java.io.File;
import java.util.Set;
import java.util.stream.Stream;

public class StandardGraphProperties implements GraphProperties {

    public StandardGraphProperties(File edgelistFile) {
        // TODO parse vertices
    }

    public StandardGraphProperties(File edgelistFile, File vertexListFile) {
        this(edgelistFile);
        // TODO parse vertices
    }

    @Override
    public long maxDegree() {
        return 0;
    }

    @Override
    public Stream<Pair<Set<String>, Set<String>>> connectedEdgePairs() {
        return null;
    }

    @Override
    public Stream<Pair<Integer, Integer>> connectedVertexDegrees() {
        return null;
    }

    @Override
    public Stream<Integer> vertexDegree() {
        return null;
    }

    @Override
    public Set<String> neighbors(String vertexId) {
        return null;
    }
}
