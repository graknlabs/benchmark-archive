# Benchmark

Benchmark is a piece of software used for measuring the performance of Grakn. It supports two main use cases:

1. Benchmarking (done using the `runner` package)
2. Visualising the benchmark result (done using the `dashboard` package)

## Requirements

1. [Download Elasticsearch](https://www.elastic.co/guide/en/elasticsearch/reference/6.3/zip-targz.html)

2. [Download Zipkin](https://github.com/openzipkin/zipkin/blob/master/zipkin-server/README.md)

3. Install `pipenv`

4. Install the requirements of `benchmark-dashboard`: 

   ```
   $ cd $BENCHMARK/dashboard
   $ pipenv install
   ```

## Using Benchmark

### 1. Start Elasticsearch & Zipkin

In the Elasticsearch installation directory, do:
```
$ ./bin/elasticsearch -E path.logs=<REPOSITORY_PATH>/data/logs/elasticsearch/ -E path.data=<REPOSITORY_PATH>/data/data/elasticsearch/
```

Check if Elasticsearch is running by accessing http://localhost:9200 from the browser.

In the Zipkin installation directory, do:

```
$ STORAGE_TYPE=elasticsearch ES_HOSTS=http://localhost:9200 ES_INDEX="benchmarking" java -jar zipkin.jar
```
Check if Zipkin is running by accessing http://localhost:9411/zipkin/ from the browser.

**NOTE**: For development purpose, you may find it easier to start Zipkin backed with an in-memory store as opposed to ElasticSearch:

```
$ java -jar zipkin.jar
```



### 2. Benchmark Grakn With Benchmark-Runner

We define YAML config files to execute under `benchmark/runner/conf/somedir`

The entry point to rebuild, generate, and name executions of config files is `run.py`

Basic usage:
`run.py --config grakn-benchmark/src/main/resources/societal_config_1.yml --execution-name query-plan-mod-1 --keyspace benchmark --ignite-dir /Users/user/Documents/benchmarking-reqs/apache-ignite-fabric-2.6.0-bin/`

Notes:

- Naming the execution not required, default name is always prepended with current Date and `name` tag in the YAML file
- Keyspace is not required, defaults to `name` in the YAML file

Further examples:

** TODO revisit run.py to see if it is needed all, was primarily intended to collect classpath, Bazel now does already **

Stop and re-unpack Grakn server, then run
`run.py --unpack-tar --config grakn-benchmark/src/main/resources/societal_config_1.yml`

Rebuild Grakn server, stop and remove the old one, untar, then run
`run.py --build-grakn --config grakn-benchmark/src/main/resources/societal_config_1.yml`

Rebuild Benchmarking and its dependencies and execute
`run.py --build-benchmark--alldeps --config grakn-benchmark/src/main/resources/societal_config_1.yml`

#### Adding new spans to measure code segments

- On the server, the intended usage is as follows:

Then add a child span that propogates in thread-local storage

```
    ScopedSpan childSpan = null;
    if (ServerTracingInstrumentation.tracingActive()) {
        childSpan = ServerTracingInstrumentation.createScopedChildSpan("newchild");
    }
    ...
    ... code to time/instrument further ...
    ...
    if (childSpan != null) {
        childSpan.finish();
    }
```

Some packages in `grakn.core` are not currently depending on `benchmark.lib` which contains the instrumentation.
In the `dependencies.yaml` make sure

```
ai.grakn:
    benchmark.lib:
        version: ...
        lang: java
```

is present, and in the BUILD file this dependency is referenced.



### 3. Visualise Results In Benchmark-Dashboard

The visualisation dashboard reads ElasticSearch and creates graphs via Dash and Plotly.

Getting it up and running requires `pipenv` and Python >=3.6.0

1. `pipenv shell` (may need to modify the `python_version = "3.6"` if the python version is newer/not quite the same. Alternatively manage the python version with `pyenv`.
2. In the `dashboard/` directory, run `python dashboard.py`
3. Navigate to `http:localhost:8050` to see the dashboard

The box plots are individually clickable to drill down, bar charts (default if only 1 repetition is being displayed) cycle through drill downs on each click.


