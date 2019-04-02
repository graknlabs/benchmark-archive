# Grakn Profiler

`profiler` is used to measure performance of code regions within Grakn in an extensible and visualisable way.

## High level usage
Input: scenarios specified by `config.yml` files (see `//common/configuration/scenario` for samples)

Output: Spans (timing blocks) created by instrumentation points within the Java client and the Grakn
server are recorded in Elasticsearch. These can be visualised using the dashboard in `//service`,
or in raw format in Zipkin's own web dashboard (port 9411).


## Running locally (not via benchmark-ci)
1. Build the benchmark profiler distribution (`bazel build //:profiler-distribution`)

2. In the unzipped distribution `external-dependencies/setup.sh` will download and start Elasticsearch and Zipkin
2. Unzip the target, and run `setup.sh` which should download and install Elasticsearch and Zipkin locally
=> Check if Zipkin is running by accessing http://localhost:9411/zipkin/ from the browser.
3. Ensure Grakn server is running *with the `--benchmark` flag*
4. In the unzipped target, run `./benchmark --config=path/to/scenario/config --execution-name=<some unique name> ... <optional args>`


## Use cases and options
1. [With Data Generation] Generate hypergraphs to different scales and profile performance

2. [Without Data Generation] Profile an existing graph in a keyspace 

3. [Without Data Generation] Profile an empty keyspace that evolves as profiled queries are committed

Further, scenario configuration YAML files offer options such as:
* concurrency - how many concurrent sessions to use, and whether they each use the same keyspace or not
* scales - what graph scales to profile at
* deleting inserted concepts - this can be enabled or disabled, as well as whether deletion should be profiled as well
* optionally comitting queries

## Adding Tracing Points

The basic component of tracing is called a Span. Spans are a component of 
Zipkin which can be nested hierarchically to create trees of execution time, 
allowing us to inspect the breakdown of code that has been instrumented.

On the Grakn Server, tracing is exposed via the `ServerTracing` class,
and should be used as follows:

```
ScopedSpan span = null;
if (ServerTracing.tracingActive()) {
    childSpan = ServerTracing.startScopedChildSpan("name");
}
...
... code to time/instrument further ...
...
if (span != null) {
    span.finish();
}
```

It's important not to forget to call `span.finish()` on all possible exit paths, otherwise
the hierarchy of spans may be assembled incorrectly. It may be necessary to combine this with `try/catch/finally`
in some cases where exceptions can lead to unexpected exit paths.
