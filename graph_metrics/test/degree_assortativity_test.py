import unittest
import numpy as np

from degree_assortativity import DegreeAssortativity


class DegreeAssortativityTest(unittest.TestCase):

    def setUp(self):
        self.assortativity = DegreeAssortativity()


    def test_joint_degree_distribution_binary(self):

        double_adjacency = {
            1: [],
            2: [],
            3: [],
            4: [5, 6], # deg 2
            5: [4, 6],
            6: [4, 5],
            7: [8, 9, 10], # deg 3
            8: [7, 9, 10],
            9: [7, 8, 10],
            10: [7, 8, 9],
        }

        joint_degree_distribution = self.assortativity.joint_degree_distribution(double_adjacency)

        correct_joint_degree_distribution = np.array([
            [0, 0, 0, 0],
            [0, 0, 0, 0],
            [0, 0, 6, 0],
            [0, 0, 0, 12],
        ])/18.0

        np.testing.assert_array_almost_equal(correct_joint_degree_distribution, joint_degree_distribution)


    def test_joint_degree_distribution_unary_binary(self):

        double_adjacency = {
            1: [1], # deg 2
            2: [],
            3: [],
            4: [4, 5, 6], # deg 4
            5: [4, 6],
            6: [4, 5],
            7: [8, 9, 10], # deg 3
            8: [7, 9, 10],
            9: [7, 8, 10],
            10: [7, 8, 9],
        }

        joint_degree_distribution = self.assortativity.joint_degree_distribution(double_adjacency)

        correct_joint_degree_distribution = np.array([
            [0, 0, 0, 0, 0],
            [0, 0, 0, 0, 0],
            [0, 0, 3, 0, 2],
            [0, 0, 0, 12, 0],
            [0, 0, 2, 0, 1],
        ])/20.0

        np.testing.assert_array_almost_equal(correct_joint_degree_distribution, joint_degree_distribution)
