package grakn.benchmark.metric;

import org.apache.commons.math3.util.Pair;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * Implements the global transitivity measure extended to hypergraphs
 * as given in
 * "Properties of metabolic graphs: Biological organization or representation artifacts?"
 * Zhou & Nakhleh, 2011
 *
 * Intuitively, we're still counting the total number of "closed" triangles (defined by three vertices/entities)
 * over total number of edge pairs, with some weighting to the number of closed triangles ("extra overlap" calculation)
 */
public class GlobalTransitivity {


    public static double computeTransitivity(GraphProperties properties) {
        long pairs = 0;
        double clusteringCoefficient = 0.0;

        Iterator<Pair<Set<String>, Set<String>>> iter = properties.connectedEdgePairs(true).iterator();
        while (iter.hasNext()) {
            Pair<Set<String>, Set<String>> edgePair = iter.next();
            clusteringCoefficient += extraOverlap(properties, edgePair.getFirst(), edgePair.getSecond());
            pairs++;
        }

        return clusteringCoefficient/pairs;
    }

    private static double extraOverlap(GraphProperties properties, Set<String> edge1, Set<String> edge2) {
        // diff1 = e1 \ e2
        Set<String> edgeDiff1 = (new HashSet<>(edge1));
        edgeDiff1.removeAll(edge2);

        // diff2 = e2 \ e1
        Set<String> edgeDiff2 = (new HashSet<>(edge2));
        edgeDiff2.removeAll(edge1);

        // neighbors of edgeDiff1
        Set<String> neighborsEdgeDiff1 = neighborsOfSet(properties, edgeDiff1);
        // neighbors of edgeDiff2
        Set<String> neighborsEdgeDiff2 = neighborsOfSet(properties, edgeDiff2);

        double cardinalityEdgeDiff1 = edgeDiff1.size();
        double cardinalityEdgeDiff2 = edgeDiff2.size();

        // effectively:
        // (neighborsEdgeDiff1 intersection edgeDiff2).size()
        neighborsEdgeDiff1.retainAll(edgeDiff2);
        long cardinalityIntersection1 = neighborsEdgeDiff1.size();

        // effectively:
        // (neighborsEdgeDiff2 intersection edgeDiff1).size()
        neighborsEdgeDiff2.retainAll(edgeDiff1);
        long cardinalityIntersection2 = neighborsEdgeDiff2.size();

        return (cardinalityIntersection1 + cardinalityIntersection2) / (cardinalityEdgeDiff1 + cardinalityEdgeDiff2);
    }

    private static Set<String> neighborsOfSet(GraphProperties properties, Set<String> idSet) {
        Set<String> neighbors = new HashSet<>();
        for (String id : idSet) {
            neighbors.addAll(properties.neighbors(id));
        }
        return neighbors;
    }
}
