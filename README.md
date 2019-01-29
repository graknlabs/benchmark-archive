# Benchmark

Benchmark is a piece of software used for generating data and measuring the performance of Grakn. It is composed of two main components:

1. *GraknBenchmark*, used to run one of the two use cases (done using the `runner` package):

       1. Generate data to different scales and profile performances with different sizes of graph

       2. Profile an existing graph
2. *Web Dashboard*, used for visualising the benchmark results (done using the `dashboard` package)

## Requirements

1. [Download Elasticsearch](https://www.elastic.co/guide/en/elasticsearch/reference/6.3/zip-targz.html)

2. [Download Zipkin](https://github.com/openzipkin/zipkin/blob/master/zipkin-server/README.md)

3. Install `pipenv` (for dashboard)

4. Install the requirements of `benchmark-dashboard`: 

   ```
   $ cd $BENCHMARK/dashboard
   $ pipenv install
   ```

## Quickstart

We use temporary helper scripts to start Benchmark from scratch on a local machine:

 1. `external-dependencies/setup.sh` will download and start Elasticsearch and Zipkin
 2. `./benchmark --config=path-to-config` will start generating data and profiling graph at different scales
 3. `./benchmark --no-data-generation -k an-existing-keyspace-name` will profile an existing keyspace, skipping the data generation

NOTE: for the time being you will need to manually kill Elasticsearch and Zipkin once you're done with benchmakring. (`jps` followed by `kill ...` should do)

## Using Benchmark

### 1. Start Elasticsearch & Zipkin

In the Elasticsearch installation directory, do:
```
$ ./bin/elasticsearch -E path.logs=<REPOSITORY_PATH>/data/elasticsearch/logs -E path.data=<REPOSITORY_PATH>/data/data/elasticsearch/
```

Check if Elasticsearch is running by accessing http://localhost:9200 from the browser.

In the Zipkin installation directory, do:

```
$ STORAGE_TYPE=elasticsearch ES_HOSTS=http://localhost:9200 ES_INDEX="benchmark" java -jar zipkin.jar
```
Check if Zipkin is running by accessing http://localhost:9411/zipkin/ from the browser.

**NOTE**: For development purpose, you may find it easier to start Zipkin backed with an in-memory store as opposed to ElasticSearch:

```
$ java -jar zipkin.jar
```



### 2. Benchmark With GraknBenchmark

We define YAML config files to execute under `benchmark/conf/somedir`

The entry point to rebuild, generate, and name executions of config files is `run.py`

Basic usage:

`benchmark --config grakn-benchmark/src/main/resources/societal_config_1.yml --execution-name query-plan-mod-1 --keyspace benchmark` 

Notes:

- Naming the execution not required, default name is always prepended with current Date and `name` tag in the YAML file
- Keyspace is not required, defaults to `name` in the YAML file

Further examples:

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

NOTE: This dashboard is the first version produced to visualize our tracing results from Zipkin.
The last expected compatible version of `benchmark` is ec2272bb2f614a424de4498e968e5b1a7497c32a
After this, the structure of spans has changed (eg. no more `batchSpan`, change order query repetitions)

The visualisation dashboard reads ElasticSearch and creates graphs via Dash and Plotly.

Getting it up and running requires `pipenv` and Python >=3.6.0

1. `pipenv shell` (may need to modify the `python_version = "3.6"` if the python version is newer/not quite the same. Alternatively manage the python version with `pyenv`.
2. In the `dashboard/` directory, run `python dashboard.py`
3. Navigate to `http:localhost:8050` to see the dashboard

The box plots are individually clickable to drill down, bar charts (default if only 1 repetition is being displayed) cycle through drill downs on each click.


