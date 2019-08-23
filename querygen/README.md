## Query Generator
This module can be used to create a diverse set of queries that are feasible according to the schema loaded
in a given keyspace. The diversity stems from the types and structure of the queries.

### Overview

The entry point, `QuerySampler`, operates a generate step followed by a reduce step. Overproducing a large population of queries
helps explore the space of possible queries widely - the larger the population, the better the coverage (as we more
or less randomly generate query structure right now). The reduce step then chooses a subset of the queries that
have diverse characteristics. The goal is to sample many possible types of queries to provide good coverage
of the query space.

In general, we specify the population to generate, and then the number of samples to produce. We also specify
the output directory and the Grakn instance and keyspace to operate against.


### Usage

Ensure that an instance of Grakn is running and accessible. Additionally, that the target keyspace has some 
schema loaded in it. 

```
bazel run //querygen:query-sampler-binary -- \
--grakn-uri=localhost:48555     \
--keyspace=MY_KEYSPACE          \
--gridded                       \
--generate=5000                 \
--sample=200                    \
--output=ABSOLUTE_TARGET_DIRECTORY
```

This will produce a `.gql` file of queries in `ABSOLUTE_TARGET_DIRECTORY`.


### Shortcomings
* attribute comparisons limited to : contains (string), <, >, == and !==
* does not have concept equality/inequality (eg. = and !=)
* no attribute values or IDs currently - good at generate structure
* no generation of `key`
* no disjunctions or negations
* other niche features: `sub!`, `isa!`, `like`, aggregates missing