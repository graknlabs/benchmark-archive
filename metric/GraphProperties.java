package grakn.benchmark.metric;

import org.apache.commons.math3.util.Pair;

import java.util.List;
import java.util.Set;

public interface GraphProperties {
     long maxDegree();

     /*
     stream the undirected edge's endpoint's degrees twice each - in each direction
     IE. an edge between a vertex of degree 1 and degree 2 produces two connected vertex degrees: (1,2) and (2,1)
      */
     List<Pair<Long, Long>> connectedVertexDegrees();
     List<Long> vertexDegree();

     /*
     Stream pairs of sets of vertex/entity IDs, each of which represents a hyperedge
     In other words, get all pairs of connected (hyper)edges sharing at least one vertex - this
     pair should only be returned once!

     if edgeCardinalityGreaterThanOne is set to true, then we ignore pure looping edges
      */
     List<Pair<Set<String>, Set<String>>> connectedEdgePairs(boolean edgeCardinalitiesGreaterThanOne);
     Set<String> neighbors(String vertexId);
}

