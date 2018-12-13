package metric;

import org.apache.commons.math3.util.Pair;

import java.util.Set;
import java.util.stream.Stream;

public interface GraphProperties {
     public int maxDegree();
     public Stream<Pair<Integer, Integer>> connectedVertexDegrees();
     public Stream<Integer> vertexDegree();
     public Stream<Pair<Set<String>, Set<String>>> connectedEdgePairs();
     public Set<String> neighbors(String vertexId);
}
