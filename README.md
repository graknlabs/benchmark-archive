# Grakn Benchmark

This repository contains components used for benchmarking and profiling Grakn.
* `common`: shared configurations and entry points for `profiler` and `report`
* `generator`: the Grakn data generator that can be used to populate a keyspace in a sophisticated manner, with configurable distributions of each type and relations
* `lib`: classes that implement Zipkin tracing for the server and Grakn Java client. Pulled in by `grakn core` and clients that have tracing enabled.
* `metric`: implementations of various more complex graph analysis metrics, compatible with Grakn or standard graphs fed from files (currently not used)
* `profiler`: the core of `benchmark-ci` that takes scenarios, generates data, and profiles queries being executed into Elasticsearch using Zipkin tracing.
* `report`: a very simplified version of `benchmark-ci` without tracing that measures high level performance of Grakn from the client side and can compile a report as a text file.
* `service`: the [dashboard](./service/dashboard), [web-server](./service/web-server) and scripts required to run `benchmark-ci`.
