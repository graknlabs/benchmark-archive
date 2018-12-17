package grakn.benchmark.metric;

import org.apache.commons.math3.util.Pair;

import java.util.Set;
import java.util.stream.Stream;

public interface GraphProperties {
     public long maxDegree();

     /*
     stream the undirected edge's endpoint's degrees twice each - in each direction
     IE. an edge between a vertex of degree 1 and degree 2 produces two connected vertex degrees: (1,2) and (2,1)
      */
     public Stream<Pair<Integer, Integer>> connectedVertexDegrees();
     public Stream<Long> vertexDegree();
     public Stream<Pair<Set<String>, Set<String>>> connectedEdgePairs();
     public Set<String> neighbors(String vertexId);
}
