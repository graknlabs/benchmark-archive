import unittest
import numpy as np
from graph_reader import GraphReader

class GraphReaderTest(unittest.TestCase):

    def test_adjacency_binary(self):
        # this is a pain to test because it depends heavily on how the python sets/dicts
        # are iterated over internally
        pass

    def test_adjacency_unary_binary(self):
        # this is a pain to test because it depends heavily on how the python sets/dicts
        # are iterated over internally
        pass

    def test_double_adjacency_binary(self):

        edge_list = np.array([
            [4, 5],
            [4, 6],
            [5, 6],
            [7, 8],
            [7, 9],
            [7, 10],
            [8, 9],
            [8, 10],
            [9, 10]
        ])
        vertices = list(range(1, 11))

        correct_double_adjacency = {
            1: set(),
            2: set(),
            3: set(),
            4: {5, 6},
            5: {4, 6},
            6: {4, 5},
            7: {8, 9, 10},
            8: {7, 9, 10},
            9: {7, 8, 10},
            10: {7, 8, 9},
        }

        reader = GraphReader(edge_list=edge_list)
        reader.add_vertices(vertices) # some vertices don't have edges
        double_adjacency = reader.double_adjacency()

        self.assertDictEqual(correct_double_adjacency, double_adjacency)


    def test_double_adjacency_unary_binary(self):

        edge_list = np.array([
            [4, 4], # loop edge
            [4, 5],
            [4, 6],
            [5, 6],
            [7, 8],
            [7, 9],
            [7, 10],
            [8, 9],
            [8, 10],
            [9, 10]
        ])
        vertices = list(range(1, 11))

        correct_double_adjacency = {
            1: set(),
            2: set(),
            3: set(),
            4: {4, 5, 6},
            5: {4, 6},
            6: {4, 5},
            7: {8, 9, 10},
            8: {7, 9, 10},
            9: {7, 8, 10},
            10: {7, 8, 9},
        }

        reader = GraphReader(edge_list=edge_list)
        reader.add_vertices(vertices) # some vertices don't have edges
        double_adjacency = reader.double_adjacency()

        self.assertDictEqual(correct_double_adjacency, double_adjacency)


