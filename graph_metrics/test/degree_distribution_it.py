import unittest
import networkx as nx
import numpy as np
from transitivity import Transitivity
from graph_reader import GraphReader

class TransitivityIt(unittest.TestCase):

    def test_degree_distribution_dissortative_network_binary(self):
        adjacency = {
            1: [],  # degree 1
            2: [],  # degree 1
            3: [1, 2],  # degree 2
            4: [],  # 1
            5: [],  # 1
            6: [4, 5, 7],  # 3
            7: [],  # 2
            8: [7, 9, 10],  # 3
            9: [10],  # 2
            10: [],  # 1
        }

        # TODO


