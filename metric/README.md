# Graph Metric Measurements

The idea of this package is to measure properties of graphs.

Currently, a small set of measures are computed:
* Degree distribution
* Degree Assortativity
* Global Transitivity

Future measures that may be added:
* Graph diameter/effective graph diameter

These measures were chosen due to their simple representations, capturing interesting properties of graphs
in few values.

Key limitations:
* Types are ignored, so the global graph structure is measured
* For now, only Entities and Relationships (~= nodes & edges) are measured. Attributes will be considered in the future,
possibly as part of the treatment of types
* Graphs are treated as being undirected and unweighted
* Multi edges are ignored

Goal:
* Produce comparable vectors of values that quantify some characteristics of graphs. Comparison my be done using
Euclidean distance or other distance measures.
* Treat any standard graphs or hypergraphs


## Degree Distribution

In general, a graph (or hypergraph) has some distribution of degrees connected to nodes. This distribution describes
the occurrence of supernodes, different modes, and variance in node degrees.

The degree distribution can be captured in various ways. Popular approaches include choosing a
few mathematical distribution laws (eg. power laws), and doing a best fit between the graph's distribution and
each of these models. This has the benefit of providing accurate models with few parameters to parametrize the distribution,
but has the downside of knowing a priori which distribution the parameters apply to. Two different graphs
with different fitted models are not comparable based on their parameters alone.

The approach taken here is to sort the distribution of degrees, and take some pre-define percentiles' values
as the discrete degree distribution. For instance, a graph with degrees of `[1, 1, 1, 1, 3, 3, 5, 10, 10, 20]` can be
roughly described using the 0th, 30th, 60th, and 100th percentile as the following vector `[1, 1, 5, 20]`. Using the same
set of percentiles from different graphs allows some comparison between them.


## Degree Assortativity

Assortativity is the likelihood of a some property to associate with similar properties. In this case, we're
interested in whether or not degrees of similar nodes link together. _High_ assortativity (approaching 1.0)
is more common in social networks, where highly connected influencers are likely to be connected to other
influencers. Animal networks and some citation networks tend to be dissortative (approaching -1.0), where
highly connected nodes connect with small nodes (~spoke-shaped graphs).

[Wikipedia](https://en.wikipedia.org/wiki/Assortativity) has a good explanation and formula. The implementation
used here follows Networkx's internal implementation of degree assortativity.

Hypergraphs are a natural extension to normal graphs since hyperedges are treated the same in terms of degree count as normal edges.
Self-edges (loops) count as multiple degrees, rather than 1, depending on how many ends of the edge point to the same entity.

## Global Transitivity

Intuitively, transitivity is the likelihood that given some vertices `a, b, c`, and two edges `ab` and `ac`,
how probable is the edge `bc`. As such, it can be computed as the ratio of _complete triangles_ to _all connected edge pairs_.
An extension to hypergraphs is presented in [Zhou & Nakhleh 2011] (https://bmcbioinformatics.biomedcentral.com/track/pdf/10.1186/1471-2105-12-132),
which is implemented here. It follows the intuition of "fraction of complete triangles out of possible triangles" and
reduces to the standard formulation of global transitivity in standard binary graphs.

Global transitivity is sometimes referred to as the global _clustering coefficient_. There's also the average clustering
coefficient/transitivity, which is the average of each node's local transitivity, and tends to be higher than global transitivity
in social graphs for instance.

There are some warnings about the extension to hypergraphs in [Zhou & Nakhleh 2011] (https://bmcbioinformatics.biomedcentral.com/track/pdf/10.1186/1471-2105-12-132)
as well, such as the fact the higher hyperedge cardinalities are, the faster the global clustering coefficient approaches 1.0.


# Roadmap

1. Implement metric protoypes in python, reading from available graph datasets [complete]
2. Translate metrics to java and using grakn as the source graph [in progress]
3. Extend measures to hypergraphs, deciding how to handle degree distribution [in progress]
5. Consider attributes
4. Consider typing, possibly starting with entity types only, ignoring relationship types (lots of big questions, eg how to compare across graphs?)
