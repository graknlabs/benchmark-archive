# Grakn Benchmark

This repository contains components used benchmarking and profiling Grakn.
* `common`: shared configurations and entry points for `profiler` and `report`
* `generator`: the Grakn data generator that can be used to populate a keyspace in a sophisticated manner, with configurable distributions of each type and relations
* `lib`: classes that implement Zipkin tracing for the server and Grakn Java client. Pulled in by `grakn core` and clients that have tracing enabled.
* `metric`: implementations of various more complex graph analysis metrics, compatible with Grakn or standard graphs fed from files (currently not used)
* `profiler`: the core of `benchmark-ci` that takes scenarios, generates data, and profiles queries being executed into Elasticsearch using Zipkin tracing.
* `report`: a very simplified version of `benchmark-ci` without tracing that measures high level performance of Grakn from the client side and can compile a report as a text file.
* `service`: the dashboard and scripts required to run `benchmark-ci`.



Benchmark is a piece of software used for generating data and measuring the performance of Grakn. It is composed of two main components:

1. *Profiler*, used to run one of the following use cases (accessed using the `profiler` package):

       
2. *Benchmark Service*, (accessed using the `service` package), which can be used to: 
    * Spin up a GCP instance that listens to commits on a specific repository (configused using Github web hooks)
    * Launch the Profiler and benchmark commits/PRs to the repository
    * Visualise tracing and other data recorded into ElasticSearch by Zipkin in a web dashboard hosted by the GCP instance





Further examples:

#### Adding new spans to measure code segments

- On the server, the intended usage is as follows:

Then add a child span that propagates in thread-local storage


