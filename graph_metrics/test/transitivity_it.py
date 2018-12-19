import unittest
import networkx as nx
import numpy as np
from transitivity import Transitivity
from graph_reader import GraphReader

def adjacency_to_edge_list(adjacency):
    """
    Helper for tests
    """
    total_edges = 0
    for edges in adjacency.values():
        total_edges += len(edges)

    edge_list = np.empty((total_edges, 2), dtype=np.uint32)
    edge_number = 0
    for start_vertex, end_vertices in adjacency.items():
        for end_vertex in end_vertices:
            edge_list[edge_number, :] = [start_vertex, end_vertex]
            edge_number += 1

    return edge_list

class TransitivityIt(unittest.TestCase):

    def test_transitivity_fully_assortative_binary(self):

        adjacency = {
            1: [],
            2: [],
            3: [],

            4: [5, 6],
            5: [6],
            6: [],

            7: [8, 9, 10],
            8: [9, 10],
            9: [10],
            10: []
        }
        edge_list = adjacency_to_edge_list(adjacency)
        reader = GraphReader(edge_list=edge_list)
        double_adjacency = reader.double_adjacency()

        transitivity_measure = Transitivity(double_adjacency=double_adjacency, edge_list=reader.edge_list)
        computed_transitivty = transitivity_measure.get_coefficient()

        # this is the expected input format in networkx
        vertex_ids = adjacency.keys()
        edge_list = reader.edge_list

        networkx_graph = nx.Graph() # using undirected graphs
        networkx_graph.add_nodes_from(vertex_ids)
        networkx_graph.add_edges_from(edge_list)
        correct_transitivity = nx.transitivity(networkx_graph)

        np.testing.assert_approx_equal(computed_transitivty, correct_transitivity)

    def test_transitivity_fully_assortative_binary(self):

        adjacency = {
            1: [2, 3],
            2: [],
            3: [],

            4: [5, 6],
            5: [6],
            6: [],

            7: [8, 9, 10],
            8: [9, 10],
            9: [10],
            10: []
        }
        edge_list = adjacency_to_edge_list(adjacency)
        reader = GraphReader(edge_list=edge_list)
        double_adjacency = reader.double_adjacency()

        transitivity_measure = Transitivity(double_adjacency=double_adjacency, edge_list=reader.edge_list)
        computed_transitivty = transitivity_measure.get_coefficient()

        # this is the expected input format in networkx
        vertex_ids = adjacency.keys()
        edge_list = reader.edge_list

        networkx_graph = nx.Graph() # using undirected graphs
        networkx_graph.add_nodes_from(vertex_ids)
        networkx_graph.add_edges_from(edge_list)
        correct_transitivity = nx.transitivity(networkx_graph)

        np.testing.assert_approx_equal(computed_transitivty, correct_transitivity)


    def test_transitivity_dissortative_network_binary(self):
        adjacency = {
            1: [], # degree 1 connnected to degree 2
            2: [], # degree 1 connected to degree 2
            3: [1, 2],  # degree 2 connected to degree 1 twice
            4: [], # 1 -> 3
            5: [], # 1 -> 3
            6: [4, 5, 7], # 3 -> 1, 1, 2
            7: [], # 2 -> 3, 4
            8: [7, 9, 10], # 4 -> 2, 1, 1, 1
            9: [10], # 1 -> 4
            10: [], # 1 -> 4
        }
        edge_list = adjacency_to_edge_list(adjacency)
        reader = GraphReader(edge_list=edge_list)
        double_adjacency = reader.double_adjacency()

        transitivity_measure = Transitivity(double_adjacency=double_adjacency, edge_list=reader.edge_list)
        computed_transitivty = transitivity_measure.get_coefficient()

        # this is the expected input format in networkx
        vertex_ids = adjacency.keys()
        edge_list = reader.edge_list

        networkx_graph = nx.Graph()  # using undirected graphs
        networkx_graph.add_nodes_from(vertex_ids)
        networkx_graph.add_edges_from(edge_list)
        correct_transitivity = nx.transitivity(networkx_graph)

        np.testing.assert_approx_equal(computed_transitivty, correct_transitivity)


    def test_transitivity_unary_binary_ignores_unary(self):
        """
        The concept of "transitivity" or "triangles" isn't definable when one edge is a self-edge, ie. the "triangle"
        is effectively composed of only two vertices A and B. We gain no information by saying "A --- A, and A -- B,
        so A -- B with likelihood x" (cf. "A -- B, B -- C, so A -- C with likelihood y").

        So choosing to ignore self-edges/loops, when measuring transitivity
        :return:
        """
        adjacency = {
            1: [],
            2: [],
            3: [1, 2, 3],   # loop
            4: [4],         # loop
            5: [5],         # loop
            6: [4, 5, 7],
            7: [],
            8: [7, 9, 10],
            9: [10],
            10: [],
        }
        edge_list = adjacency_to_edge_list(adjacency)
        reader = GraphReader(edge_list=edge_list)
        double_adjacency = reader.double_adjacency()

        transitivity_measure = Transitivity(double_adjacency=double_adjacency, edge_list=reader.edge_list)
        computed_transitivty = transitivity_measure.get_coefficient()

        # this is the expected input format in networkx
        vertex_ids = adjacency.keys()
        edge_list = reader.edge_list

        networkx_graph = nx.Graph()  # using undirected graphs
        networkx_graph.add_nodes_from(vertex_ids)
        networkx_graph.add_edges_from(edge_list)
        # networkx does ignore loops in transitivity calculations, safe to use as a reference again
        correct_transitivity = nx.transitivity(networkx_graph)

        np.testing.assert_approx_equal(computed_transitivty, correct_transitivity)
