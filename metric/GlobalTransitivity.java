package grakn.benchmark.metric;

import org.apache.commons.math3.util.Pair;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

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

        List<Pair<Set<String>, Set<String>>> connectedEdgePairs = properties.connectedEdgePairs(true);

        // parallelize the following neighbor checking since they are each checked independently and aggregated
        BlockingQueue<GraphProperties> propertiesList = new LinkedBlockingQueue<>(
                Arrays.asList(properties.copy(), properties.copy(), properties.copy(), properties.copy(), properties.copy()));

        // *** active ignore edges that are connecting the same entities ***
        List<Double> extraOverlaps = connectedEdgePairs.parallelStream()
                .filter(edgePair -> !edgePair.getFirst().equals(edgePair.getSecond()))
                .map(edgePair -> {
                    try {
                        GraphProperties propertiesInstance = propertiesList.take();
                        double overlap = extraOverlap(propertiesInstance, edgePair.getFirst(), edgePair.getSecond());
                        propertiesList.add(propertiesInstance);
                        return overlap;
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());

//        Iterator<Pair<Set<String>, Set<String>>> iter = connectedEdgePairs.iterator();
//        while (iter.hasNext()) {
//            Pair<Set<String>, Set<String>> edgePair = iter.sample();
//            clusteringCoefficient += extraOverlap(properties, edgePair.getFirst(), edgePair.getSecond());
//            pairs++;
//        }

        double extraOverlapTotal = extraOverlaps.stream().reduce(0.0, (a,b) -> a+b);
        double transitivity = extraOverlapTotal/extraOverlaps.size();

        propertiesList.stream().forEach(props -> props.close());

        return transitivity;
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
        double cardinalityIntersection1 = neighborsEdgeDiff1.size();

        // effectively:
        // (neighborsEdgeDiff2 intersection edgeDiff1).size()
        neighborsEdgeDiff2.retainAll(edgeDiff1);
        double cardinalityIntersection2 = neighborsEdgeDiff2.size();

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
