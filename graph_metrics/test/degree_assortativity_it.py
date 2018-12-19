import numpy as np
import networkx as nx
import unittest

from degree_assortativity import DegreeAssortativity
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


class DegreeAssortativityIT(unittest.TestCase):

    def setUp(self):
        self.assortativity = DegreeAssortativity()

    def test_test(self):
        adjacency = {
            1: [2, 3],
            2: [],
            3: [4],

            4: [],
            5: [],
            6: [4, 5, 7],

            7: [8],
            8: [9, 10],
            9: [10],
            10: []
        }
        edge_list = adjacency_to_edge_list(adjacency)
        reader = GraphReader(edge_list=edge_list)
        double_adjacency = reader.double_adjacency()

        computed_assortativity = self.assortativity.compute_degree_assortativity(double_adjacency)

        # this is the expected input format in networkx
        vertex_ids = adjacency.keys()
        edge_list = adjacency_to_edge_list(adjacency)

        networkx_graph = nx.Graph() # using undirected graphs
        networkx_graph.add_nodes_from(vertex_ids)
        networkx_graph.add_edges_from(edge_list)
        correct_assortativity = nx.degree_assortativity_coefficient(networkx_graph)

        np.testing.assert_approx_equal(computed_assortativity,correct_assortativity, err_msg="Assortativity score mismatch in a binary graph without self-edges, that should be fully assortative")

    def test_degree_assortativity_fully_assortative_binary(self):

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

        computed_assortativity = self.assortativity.compute_degree_assortativity(double_adjacency)

        # this is the expected input format in networkx
        vertex_ids = adjacency.keys()
        edge_list = adjacency_to_edge_list(adjacency)

        networkx_graph = nx.Graph() # using undirected graphs
        networkx_graph.add_nodes_from(vertex_ids)
        networkx_graph.add_edges_from(edge_list)
        correct_assortativity = nx.degree_assortativity_coefficient(networkx_graph)

        np.testing.assert_approx_equal(computed_assortativity,correct_assortativity, err_msg="Assortativity score mismatch in a binary graph without self-edges, that should be fully assortative")


    def test_degree_assortativity_fully_assortative_unary_binary(self):

        adjacency = {
            1: [1],
            2: [2],
            3: [3],

            4: [4, 5, 6], # with self-edge
            5: [5, 6], # with self-edge
            6: [6], # with self-edge

            7: [8, 9, 10],
            8: [9, 10],
            9: [10],
            10: []
        }
        edge_list = adjacency_to_edge_list(adjacency)
        reader = GraphReader(edge_list=edge_list)
        double_adjacency = reader.double_adjacency()


        computed_assortativity = self.assortativity.compute_degree_assortativity(double_adjacency)

        # this is the expected input format for networkx
        vertex_ids = adjacency.keys()
        edge_list = adjacency_to_edge_list(adjacency)
        networkx_graph = nx.Graph() # using undirected graphs
        networkx_graph.add_nodes_from(vertex_ids)
        networkx_graph.add_edges_from(edge_list)
        correct_assortativity = nx.degree_assortativity_coefficient(networkx_graph)


        np.testing.assert_approx_equal(computed_assortativity, correct_assortativity, err_msg="")



    def test_degree_disassortative_binary(self):
        adjacency = {
            1: [],  # degree 1 connnected to degree 2
            2: [],  # degree 1 connected to degree 2
            3: [1, 2],  # degree 2 connected to degree 1 twice
            4: [],  # 1 -> 3
            5: [],  # 1 -> 3
            6: [4, 5, 7],  # 3 -> 1, 1, 2
            7: [],  # 2 -> 3, 4
            8: [7, 9, 10],  # 4 -> 2, 1, 1, 1
            9: [10],  # 1 -> 4
            10: [],  # 1 -> 4
        }
        edge_list = adjacency_to_edge_list(adjacency)
        reader = GraphReader(edge_list=edge_list)
        double_adjacency = reader.double_adjacency()

        computed_assortativity = self.assortativity.compute_degree_assortativity(double_adjacency)

        # this is the expected input format for networkx
        vertex_ids = adjacency.keys()
        edge_list = adjacency_to_edge_list(adjacency)

        networkx_graph = nx.Graph()  # using undirected graphs
        networkx_graph.add_nodes_from(vertex_ids)
        networkx_graph.add_edges_from(edge_list)
        correct_assortativity = nx.degree_assortativity_coefficient(networkx_graph)

        np.testing.assert_approx_equal(computed_assortativity, correct_assortativity, err_msg="")


    def test_degree_disassortative_unary_binary(self):
        adjacency = {
            1: [], # degree 1 connnected to degree 3
            2: [], # degree 1 connected to degree 3
            3: [3, 1, 2], # degree 4 connected to degree 1 twice
            4: [], # 1 -> 3
            5: [], # 1 -> 3
            6: [4, 5, 7], # 3 -> 1, 1, 2
            7: [], # 2 -> 3, 4
            8: [7, 9, 10, 11], # 4 -> 2, 1, 1, 1
            9: [], # 1 -> 4
            10: [], # 1 -> 4
            11: [] # 1 -> 4
        }

        edge_list = adjacency_to_edge_list(adjacency)
        reader = GraphReader(edge_list=edge_list)
        double_adjacency = reader.double_adjacency()

        computed_assortativity = self.assortativity.compute_degree_assortativity(double_adjacency)

        # this is the expected input format for networkx
        vertex_ids = adjacency.keys()
        edge_list = adjacency_to_edge_list(adjacency)

        networkx_graph = nx.Graph()  # using undirected graphs
        networkx_graph.add_nodes_from(vertex_ids)
        networkx_graph.add_edges_from(edge_list)
        correct_assortativity = nx.degree_assortativity_coefficient(networkx_graph)

        np.testing.assert_approx_equal(computed_assortativity, correct_assortativity, err_msg="")
