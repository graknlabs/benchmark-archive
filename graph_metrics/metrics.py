import argparse
import numpy as np

from graph_reader import GraphReader
from degree_distribution import DegreeDistribution
from degree_assortativity import DegreeAssortativity
from clustering_coefficient import ClusteringCoefficient

# plotting histograms
# import matplotlib
# matplotlib.use('Agg')
# import matplotlib.pyplot as plt

parser = argparse.ArgumentParser("Compute various metrics over an input graph of directed edges between node IDs")
parser.add_argument("--graph", type=str, required=True, help="Input  tab-separated edge list, comments with # are ignored ")

args = parser.parse_args()
graph_file = args.graph

graph_reader = GraphReader(edge_list_file=graph_file)
vertices = graph_reader.vertices
sub_vertices = np.random.choice(list(vertices), int(len(vertices)/2), replace=False)
print("Reduced graph from {1} to {0} vertices".format(len(sub_vertices), len(vertices)))
graph_reader = graph_reader.subgraph(sub_vertices)
double_adjacency = graph_reader.double_adjacency()

# degree distribution
percentiles = [0, 25, 50, 75, 100]
degree_distribution_metric = DegreeDistribution(double_adjacency=double_adjacency)
percentile_degrees = degree_distribution_metric.discretized_degree_distribution(percentiles=percentiles)
print("Corresponding degree & percentile: {0}".format(", ".join(["{0} - {1}%".format(deg, perc) for (deg, perc) in zip(percentile_degrees, percentiles)])))



# assortativity
assortativity = DegreeAssortativity()
print("Degree assortativity: {0}".format(assortativity.compute_degree_assortativity(double_adjacency=double_adjacency)))


# clustering coefficient
cc = ClusteringCoefficient(double_adjacency=double_adjacency, edge_list=graph_reader.edge_list)
print("Clustering coefficient: {0}".format(cc.get_coefficient()))


# fig = plt.hist(vertex_out_degree, bins=int(vertex_out_degree.shape[0]*HIST_BINS_PROPORTION), label="Vertex out degree")
# plt.savefig("vertex_out_degree_{0}".format(graph_file.split(".")[0]))
# plt.clf()

