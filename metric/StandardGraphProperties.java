package grakn.benchmark.metric;

import org.apache.commons.math3.util.Pair;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StandardGraphProperties implements GraphProperties {

    private HashMap<String, Set<String>> doubleAdjacencyList;

    /**
     * Read a possibly commented (#) CSV/TSV edge list file, listing edges as vertex lists separated by `separator`
     * @param edgelistFilePath
     * @param separator vertex separator in each line
     * @throws IOException
     */
    public StandardGraphProperties(Path edgelistFilePath, char separator) throws IOException {

        List<Pair<String, String>> edgeList = Files.lines(edgelistFilePath)
                // ignore lines that are commented or empty
                .filter(line -> !line.startsWith("#") && line.trim().length() > 0)
                // split and collect to Set, while trimming whitespace (\\s* is a whitespace consuming regex)
                .map(line -> {
                    String[] vertices = line.split("\\s*" + separator + "\\s*");
                    return new Pair<>(vertices[0], vertices[1]);
                })
                .collect(Collectors.toList());

        Set<String> vertexIds = new HashSet<>();
        for (Pair<String, String> pair : edgeList) {
            vertexIds.add(pair.getFirst());
            vertexIds.add(pair.getSecond());
        }
        // initialize double adjacency lists
        doubleAdjacencyList = new HashMap<>(vertexIds.size());
        for (String id : vertexIds) {
            doubleAdjacencyList.put(id, new HashSet<>());
        }

        // convert edge list to double adjacency lists for easier/faster accesses
        for (Pair<String, String> edge : edgeList) {
            String start = edge.getFirst();
            String end = edge.getSecond();
            doubleAdjacencyList.get(start).add(end);
            doubleAdjacencyList.get(end).add(start);
        }
    }

//    public StandardGraphProperties(File edgelistFile, File vertexListFile) {
//        this(edgelistFile);
//        // TODO parse vertices
//    }

    @Override
    public long maxDegree() {
        return this.vertexDegree().max(Comparator.naturalOrder()).orElse(0l);
    }

    @Override
    public Stream<Pair<Set<String>, Set<String>>> connectedEdgePairs() {
        return null;
    }

    @Override
    public Stream<Pair<Integer, Integer>> connectedVertexDegrees() {
        return null;
    }

    /**
     * Stream the degree of each vertex
     * NOTE: in graph theory, a self-edge (loop) counts as +2 (or +n for self-hyperedges)
     * In this case, we only have +2 for self edges since we don't allow hyperedges
     * @return
     */
    @Override
    public Stream<Long> vertexDegree() {
        return this.doubleAdjacencyList.entrySet().stream().map(
                entrySet -> {
                    String vertexId = entrySet.getKey();
                    Set<String> neighbors = entrySet.getValue();
                    long degree = neighbors.size();
                    if (neighbors.contains(vertexId)) {
                        degree++;
                    }
                    return degree;
                }
        );
    }

    @Override
    public Set<String> neighbors(String vertexId) {
        return this.doubleAdjacencyList.get(vertexId);
    }
}
