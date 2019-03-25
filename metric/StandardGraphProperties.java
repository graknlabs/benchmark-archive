/*
 *  GRAKN.AI - THE KNOWLEDGE GRAPH
 *  Copyright (C) 2019 Grakn Labs Ltd
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package grakn.benchmark.metric;

import org.apache.commons.math3.util.Pair;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

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

    public StandardGraphProperties(HashMap<String, Set<String>> doubleAdjacencyList) {
        this.doubleAdjacencyList = doubleAdjacencyList;
    }

    @Override
    public StandardGraphProperties copy() {
        return new StandardGraphProperties(doubleAdjacencyList);
    }

    @Override
    public void close() {
         return;
    }

    @Override
    public long maxDegree() {
        return this.vertexDegree().stream().max(Comparator.naturalOrder()).orElse(0l);
    }

    /**
     * Stream pairs of intersecting edges (ie. share at least one vertex)
     * if requireAtLeastThreeUniqueVertices is true, only return pairs of edges
     * that involve separate vertices (say, A, B, C), because sometimes we may wish to ignore
     * looping edges (self-edges)
     *
     * In the case of a triangle (A, B, C fully connected), each combination of pairs will be returned once
     * (A -- B, B -- C), (B -- A, A -- C), (B -- C, C -- A)
     * @return
     */
    @Override
    public List<Pair<Set<String>, Set<String>>> connectedEdgePairs(boolean edgeCardinalitiesGreaterThanOne) {
        return doubleAdjacencyList.entrySet().stream()
            .map(
                stringSetEntry -> {
                    String start = stringSetEntry.getKey();
                    Set<String> neighbors = stringSetEntry.getValue();
                    List<Pair<Set<String>, Set<String>>> neighborPairs = combinations(start, neighbors, edgeCardinalitiesGreaterThanOne);
                    return neighborPairs.stream();
                }
            )
            .flatMap(e -> e)
            .collect(Collectors.toList());
    }

    /**
     * Example: Giveen element `e`, set `set`: find all 2-combinations of `set` -> (x,y), then
     * return pairs with the given element `e` as [(e, x), (e, y)]
     *
     * @param element The element that must be present in each returned set
     * @param set The set that we draw all possible two-combinations from
     * @param requireUniqueElements require that `e`, `x` and `y` are all different
     * @return
     */
    private List<Pair<Set<String>, Set<String>>> combinations(String element, Set<String> set, boolean requireUniqueElements) {
        LinkedList<Pair<Set<String>, Set<String>>> combinations = new LinkedList<>();
        // convert set to array to iterate over in order
        String[] orderedSet = set.toArray(new String[set.size()]);
        for (int i = 0; i < orderedSet.length; i++) {
            String e1 = orderedSet[i];
            for (int j = i+1; j < orderedSet.length; j++) {
                String e2 = orderedSet[j];

                if (requireUniqueElements && (element.equals(e1) || element.equals(e2) || e1.equals(e2))) {
                    continue;
                }

                Set<String> firstSet = new HashSet<>();
                firstSet.add(element);
                firstSet.add(e1);

                Set<String> secondSet = new HashSet<>();
                secondSet.add(element);
                secondSet.add(e2);

                combinations.add(new Pair<>(firstSet, secondSet));
            }
        }
        return combinations;
    }

    /**
     * Return degrees of pairs of connected vertices
     * ie. for all connected vertices A, B, return (deg(A), deg(B))
     * we actually return each undirected edge twice,
     * so for connected vertices A,B, both (deg(A), deg(B) and (deg(B), deg(A)) are returned
     *
     * Self-edges count as degree +2 instead of +1
     * @return
     */
    @Override
    public List<Pair<Long, Long>> connectedVertexDegrees() {
        return doubleAdjacencyList.entrySet().stream()
            .map(
                stringSetEntry -> {
                    String start = stringSetEntry.getKey();
                    Set<String> neighbors = stringSetEntry.getValue();
                    Long startDegree = neighbors.contains(start) ? 1l + neighbors.size() : neighbors.size();

                    return neighbors.stream().map(end -> {
                        Set<String> endNeighbors = doubleAdjacencyList.get(end);
                        Long endDegree = endNeighbors.contains(end) ? 1l + endNeighbors.size() : endNeighbors.size();
                        return new Pair<>(startDegree, endDegree);
                    });
                }
            )
            .flatMap(e -> e)
            .collect(Collectors.toList());
    }

    /**
     * Stream the degree of each vertex
     * NOTE: in graph theory, a self-edge (loop) counts as +2 (or +n for self-hyperedges)
     * In this case, we only have +2 for self edges since we don't allow hyperedges
     * @return
     */
    @Override
    public List<Long> vertexDegree() {
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
        ).collect(Collectors.toList());
    }

    @Override
    public Set<String> neighbors(String vertexId) {
        return this.doubleAdjacencyList.get(vertexId);
    }
}
