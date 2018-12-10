import numpy as np




class DegreeDistribution():

    def __init__(self, double_adjacency):
        self.double_adjacency = double_adjacency

        self.degrees = []
        for vertex, neighbors in self.double_adjacency.items():
            if vertex in neighbors:
                self.degrees.append(len(neighbors)+1)
            else:
                self.degrees.append(len(neighbors))

        self.degrees = np.array(self.degrees)
        self.degrees.sort()

        # count the number of each degree
        # degree_counts = np.bincount(self.degrees)
        # self.degree_degree_counts = np.vstack([np.arange(degree_counts.shape[0]), degree_counts]).T

    def discretized_degree_distribution(self, percentiles=[0, 25, 50, 75, 100]):

        # Takes a CONTINUOUS sorted_degrees parameter ie. no gaps - 0s are included
        # max_degree = self.degree_degree_counts.shape[0] - 1
        # indices = max_degree * 0.01 * np.array(percentiles)
        # indices = np.rint(indices)
        # # one issue is that the indexes might be == length, which is out of bounds as an index by 1
        # indices = np.clip(indices, 0, max_degree - 1)
        # return self.degree_degree_counts[indices, :]


        num_degrees = self.degrees.shape[0]
        indices = num_degrees * 0.01 * np.array(percentiles)
        indices = np.rint(indices)
        # one issue is that the indexes might be == length, which is out of bounds as an index by 1
        indices = np.clip(indices, 0, num_degrees - 1).astype(np.uint32)
        return self.degrees[indices]