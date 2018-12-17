import numpy as np


class DegreeAssortativity(object):

    def compute_degree_assortativity(self, double_adjacency):

        # method following networkx's internal implementation
        # generate the full matrix of degree -- degree connectivity counts
        # ie. joint probability distribution of connected vertices' degrees
        # normalize this across the whole matrix/distribution (not this implicitly counts edges twice unless its a loop)
        # then using this, compute required q_k distribution by
        # summing columns or rows (symmetric matrix for undirected edge case we have)
        #
        # can use this matrix & q_k to compute variance(q), and the required sum in r
        # for reference, follow from
        # https://networkx.github.io/documentation/stable/_modules/networkx/algorithms/assortativity/correlation.html#degree_assortativity_coefficient


        joint_degree_distribution = self.joint_degree_distribution(double_adjacency)

        degrees = np.arange(joint_degree_distribution.shape[0])

        # q follows from the property that the sum of any column j (or row since symmetric) = q_j
        q = np.sum(joint_degree_distribution, axis=0)

        mean_q = np.dot(degrees, q)
        var_q = np.sum(q * (degrees - mean_q)**2)

        # do the summation
        # could do in numpy the same as as networkx,
        # however not as portable to future java implementations

        r = 0.0
        for deg_j in degrees:
            q_j = q[deg_j]
            for deg_k in degrees:
                q_k = q[deg_k]
                r += deg_j * deg_k * (joint_degree_distribution[deg_j, deg_k] - q_j*q_k)

        return r/var_q

    def joint_degree_distribution(self, double_adjacency):
        """
        Produces a matrix that is the joint probability distribution of having (degree x, degree y)
        as given by the edges in the graph.
        Note this is a symmetric matrix, so when normalizing the matrix edges that are NOT loops are double counted.
        From the networkx implementation, this appears to be valid and produces valid results on the examples I did by hand.

        :param double_adjacency: adjacency lists that lists both ends of an edge ie. { start: [...end...], end: [...start...]}
        :return: joint probability matrix of vertices of degree (m,n) being connected by an edge
        """

        # NOTE need to double count self-edge as degree +2!!!

        highest_degree = np.max([1+len(neighbors) if vertex in neighbors else len(neighbors) for vertex,neighbors in double_adjacency.items()])

        distribution = np.zeros((highest_degree+1, highest_degree+1))

        for start, neighbors in double_adjacency.items():
            start_degree = 1+len(neighbors) if start in neighbors else len(neighbors)
            for end in neighbors:
                end_degree = 1+len(double_adjacency[end]) if end in double_adjacency[end] else len(double_adjacency[end])
                distribution[start_degree, end_degree] += 1

        return distribution/distribution.sum()
