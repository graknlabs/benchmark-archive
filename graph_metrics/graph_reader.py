import numpy as np


class GraphReader:
    """
    Reads undirected graph edge lists between two vertices (NOT hypergraphs)

    if a directed graph is passed (explicitly lists some edges in both directions), these edges
    bidirectional edges are converted into a single undirected edge
    """

    def __init__(self, edge_list_file=None, edge_list=None, deduplicate_directed_edges=True):
        if edge_list is None and edge_list_file is None:
            raise Exception("Must provide either `edge_list_file` or `edge_list`")

        if deduplicate_directed_edges:
            if edge_list_file is not None:
                edge_set = self._read_and_deduplicate_edge_list_file_to_set(edge_list_file)
            elif edge_list is not None:
                edge_set = set([frozenset(edge) for edge in edge_list])
            self.edge_list = self._edge_set_to_edge_list(edge_set)
        else:
            if edge_list_file is not None:
                edge_list = self._read_raw_edge_list_file(edge_list_file)
            # otherwise, just use the edge_list provided
            self.edge_list = edge_list

        self.vertices = set(self.edge_list.ravel())


    def _read_raw_edge_list_file(self, edge_list_file):
        total_edges = 0
        # first pass: read number of edges
        with open(edge_list_file) as f:
            for line in f:
                if len(line.strip()) != 0 and not line.startswith("#"):
                    total_edges += 1

        edge_list = np.empty((total_edges, 2))

        with open(edge_list_file) as f:
            for i, line in enumerate(f):
                if len(line.strip()) == 0 or line.startswith("#"):
                    continue
                if '\t' in line:
                    line = line.split("\t")
                elif ',' in line:
                    line = line.split(',')
                else:
                    line = line.split(" ")
                edge = [int(v.strip()) for v in line if len(v.strip()) > 0]
                edge_list[i, :] = edge
        return edge_list

    def _read_and_deduplicate_edge_list_file_to_set(self, edge_list_file):
        edge_set = set()
        with open(edge_list_file) as f:
            for line in f:
                if len(line.strip()) == 0 or line.startswith("#"):
                    continue
                if '\t' in line:
                    line = line.split("\t")
                elif ',' in line:
                    line = line.split(',')
                else:
                    line = line.split(" ")

                edge = frozenset([int(v.strip()) for v in line if len(v.strip()) > 0])
                edge_set.add(edge) # if it already exists, it doesn't get duplicated due to use of sets
        return edge_set


    def _edge_set_to_edge_list(self, edge_set):
        """ Convert edge set to edge list """
        num_edges = len(edge_set)
        edge_list = np.empty((num_edges, 2))
        # convert to numpy array
        for i, edge in enumerate(edge_set):
            edge_list[i, :] = list(edge)
        return edge_list


    def add_vertices(self, vertex_ids):
        self.vertices.update(vertex_ids)

    def subgraph(self, subgraph_vertex_ids_set):
        edge_list = []
        for edge in self.edge_list:
            for vertex in edge:
                if vertex not in subgraph_vertex_ids_set:
                    break
            else: # if all vertices are in the allowed subgraph, then save the edge
                edge_list.append(edge)

        return GraphReader(edge_list=np.array(edge_list, dtype=np.uint32))

    def edge_list(self):
        return self.edge_list

    def num_edges(self):
        return self.edge_list.shape[0]

    def num_vertices(self):
        return len(self.vertices)

    def adjacency(self):
        """
        Return adjacency list of undirected edges in form of a python dictionary
        Edges only included in one direction
        """
        adjacency = {}
        for v in self.vertices:
            adjacency[v] = set()

        for (start, end) in self.edge_list:
            adjacency[start].add(end)

        return adjacency

    def double_adjacency(self):
        """
        Return adjacency list of undirected edges in form of a python dictionary
        Edges only included in one direction
        """
        # adjacency = {v:set() for v in self.vertices}

        # this is faster
        adjacency = dict.fromkeys(self.vertices)
        for v in adjacency:
            adjacency[v] = set()

        for (start, end) in self.edge_list:
            adjacency[start].add(end)
            if start != end:
                adjacency[end].add(start)

        return adjacency


