from itertools import combinations

class Transitivity:
    """
        Implementation of generalized hypergraph clustering coefficient from Dewar 2017
        Currently assumes adjacency list is actually just binary edges (ie. collection of neighbor vertices,
        rather than collection of collections of vertices)
    """

    def __init__(self, double_adjacency, edge_list):

        self.double_adjacency = double_adjacency # TODO update to handle hyperedge adjacencies
        self.edge_list = edge_list

    def get_coefficient(self):

        pairs = 0
        clustering_total = 0.0
        for (edge1, edge2) in self._stream_intersecting_edge_pairs():
            pairs += 1
            clustering_total += self._extra_overlap(edge1, edge2)

        return clustering_total/pairs


    def _stream_intersecting_edge_pairs(self):
        """
        Stream intersecting edge pairs, that also involve more than two different vertices!
        :return:
        """
        counter = 0
        total = self._possible_triangles()
        for start, neighbors in self.double_adjacency.items():
            # stream pairwise edges from this start vertex
            for (end1, end2) in combinations(neighbors, 2):
                if start == end1 or start == end2:
                    continue
                if counter % 500000 == 0:
                    print("Completed {0}/{1} --- {2:.2f}%".format(counter, total, 100*counter/total))
                counter += 1
                yield (set((start, end1)), set((start, end2)))

    def _possible_triangles(self):
        """
        Small helper to count number of possible triangles that will be streamed as pairwise edges. Mostly for
        printing progress
        TODO needs to be  reconsidered for hyperedges (possible triangles increases!) (or not, since we only use this for printing)

        """
        poss = 0
        for adj in self.double_adjacency.values():
            n_neighbors = len(adj)
            n_pairs = n_neighbors * (n_neighbors - 1) / 2
            poss += n_pairs
        return poss


    def _extra_overlap(self, edge1, edge2):
        e1 = edge1
        e2 = edge2

        diff1 = self._set_subtract(e1, e2)
        diff2 = self._set_subtract(e2, e1)

        neighbors_diff1 = self._neighbors_of(diff1)
        neighbors_diff2 = self._neighbors_of(diff2)

        # for undirected binary graphs, I'm 99% sure that the two intersections are always the same
        # so technically we don't need to compute both
        # however, when generalizing to hypergraphs this becomes necessary so we keep it for consistency

        return ( len(neighbors_diff1.intersection(diff2)) + len(neighbors_diff2.intersection(diff1)) ) / (len(diff1) + len(diff2))
        # return ( len(neighbors_diff1.intersection(diff2)) *2) / (len(diff1) + len(diff2))


    def _set_subtract(self, a, b):
        # return a - a.intersection(b)
        return a - b

    def _neighbors_of(self, vertex_set):
        neighbors = set()
        for vertex in vertex_set:
            neighbors |= self.double_adjacency[vertex]

        return neighbors

