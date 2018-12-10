import numpy as np


class GraphReader:
    """
    Reads undirected graph edge lists & processes them
    """

    def __init__(self, edge_list_file=None, edge_list=None):

        # parse edge list file into edge_list if a file is provided
        if edge_list_file is not None:
            # count number of lines in the edge list file first
            with open(edge_list_file) as f:
                num_edges = 0
                for line in f:
                    if not (line.startswith("#") or len(line.strip()) == 0):
                        num_edges += 1

            edge_list = np.empty([num_edges, 2], dtype=np.uint32)

            # construct edge list in numpy array
            with open(edge_list_file) as f:
                count = 0
                for line in f:
                    if len(line.strip()) == 0 or line.startswith("#"):
                        continue

                    if '\t' in line:
                        line = line.split("\t")
                    elif ',' in line:
                        line = line.split(',')
                    else:
                        line = line.split(" ")

                    vertices = [int(v.strip()) for v in line if len(v.strip()) > 0]
                    edge_list[count, :] = vertices
                    count += 1

        assert isinstance(edge_list, np.ndarray)
        self.edge_list = edge_list
        self.vertices = set(self.edge_list.ravel())

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


