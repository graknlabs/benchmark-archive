# Data Generator

The Grakn-specific data generator for creating complex hypergraphs dynamically.

## Usage
The library is intended to be used coupled with other entry points. The main operation
that can be called is `generate(limit)`, which writes to the indicated keyspace
until the scale of the hypergraph exceeds the given limit.

The constructor also takes in a parameter `dataGenerator`, which selects
which `definition` is used to generate the graph. The definition is a manually-written
specification of how many of each type of entity, relationship and attribute to 
generate, and how to connect them.

