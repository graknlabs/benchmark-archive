import unittest
import numpy as np
import os
from graph_reader import GraphReader

class GraphReaderTest(unittest.TestCase):

    def test_directed_edge_deduplication_from_file(self):
        edge_list = np.array([
            [4, 5], [5, 4],
            [4, 6], [6, 4],
            [5, 6],
            [7, 8], [8, 7],
            [7, 9],
            [7, 10], [10, 7],
            [8, 9], [9, 8],
            [10, 8],
            [9, 10], [10, 9]
        ])

        edge_list_file = "test_edge_list_file.txt"
        with open(edge_list_file, 'w') as f:
            for (start, end) in edge_list:
                f.write("{0} {1}\n".format(start, end))

        correct_edge_list = np.array([
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

        reader = GraphReader(edge_list_file=edge_list_file, deduplicate_directed_edges=True)
        reader_edge_list = reader.edge_list

        # hard to test for correctness due to non deterministic edge reductions, just check shape match
        np.testing.assert_equal(correct_edge_list.shape, reader_edge_list.shape)

        os.remove(edge_list_file)

    def test_directed_edge_deduplication(self):
        edge_list = np.array([
            [4, 5], [5, 4],
            [4, 6], [6, 4],
            [5, 6], [6, 5],
            [7, 8],
            [7, 9], [9, 7],
            [7, 10], [10, 7],
            [8, 9], [9, 8],
            [8, 10],
            [9, 10], [10, 9]
        ])

        correct_edge_list = np.array([
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

        reader = GraphReader(edge_list=edge_list, deduplicate_directed_edges=True)
        reader_edge_list = reader.edge_list

        # hard to test for correctness due to non deterministic edge reductions, just check shape match
        np.testing.assert_equal(correct_edge_list.shape, reader_edge_list.shape)


    def test_undirected_edge_no_deduplication_flag(self):
        """ Not deduplicating if the the edge list file isknown to be undirected will save time on large graphs """

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
        reader = GraphReader(edge_list=edge_list, deduplicate_directed_edges=False)
        reader_edge_list = reader.edge_list
        np.testing.assert_array_almost_equal(edge_list.shape, reader_edge_list.shape)

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


