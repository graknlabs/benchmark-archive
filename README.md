# Benchmarking

To get started, Grakn, Ignite, Elasticsearch and Zipkin need to be running.

## Elasticsearch
https://www.elastic.co/guide/en/elasticsearch/reference/6.3/zip-targz.html

In the elasticsearch installation directory, do:
```
./bin/elasticsearch -E path.logs=[REPOSITORY_PATH]/data/logs/elasticsearch/ -E path.data=[REPOSITORY_PATH]/data/data/elasticsearch/
```

## Zipkin
https://github.com/openzipkin/zipkin/blob/master/zipkin-server/README.md

In the zipkin installation directory, do:

```
STORAGE_TYPE=elasticsearch ES_HOSTS=http://localhost:9200 ES_INDEX="benchmarking" java -jar zipkin.jar
```
The above connects to a running Elasticsearch backend, which persists benchmarking data

To start without using Elasticsearch, do:
```
java -jar zipkin.jar
```

Access zipkin to see the spans recorded at: http://localhost:9411/zipkin/

Check elasticsearch is running by receiving a response from http://localhost:9200 in-browser

## Plotly Dashboard

The visualisation dashboard reads ElasticSearch and creates graphs via Dash and Plotly.

Getting it up and running requires pipenv and python >=3.6.0

1. `pipenv install` (installs package dependencies for the dashboard)
2. `pipenv shell` (may need to modify the `python_version = "3.6"` if the python version is newer/not quite the same. Alternatively manage the python version with `pyenv`.
3. In the `dashboard/` directory, run `python dashboard.py`
4. Navigate to `http:localhost:8050` to see the dashboard

The box plots are individually clickable to drill down, bar charts (default if only 1 repetition is being displayed) cycle through drill downs on each click.

## Executing Benchmarks and Generating Data

We define YAML config files to execute under `benchmark/runner/conf/somedir`

The entry point to rebuild, generate, and name executions of config files is `run.py`

Basic usage:
`run.py --config grakn-benchmark/src/main/resources/societal_config_1.yml --execution-name query-plan-mod-1 --keyspace benchmark --ignite-dir /Users/user/Documents/benchmarking-reqs/apache-ignite-fabric-2.6.0-bin/`

Notes:
* Naming the execution not required, default name is always prepended with current Date and `name` tag in the YAML file
* Keyspace is not required, defaults to `name` in the YAML file

Further examples:


** TODO revisit run.py to see if it is needed all, was primarily intended to collect classpath, Bazel now does already **


Stop and re-unpack Grakn server, then run
`run.py --unpack-tar --config grakn-benchmark/src/main/resources/societal_config_1.yml`

Rebuild Grakn server, stop and remove the old one, untar, then run
`run.py --build-grakn --config grakn-benchmark/src/main/resources/societal_config_1.yml`

Rebuild Benchmarking and its dependencies and execute
`run.py --build-benchmark--alldeps --config grakn-benchmark/src/main/resources/societal_config_1.yml`


### Adding new spans to measure code segments

* On the server, the intended usage is as follows:

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




## Kibana
Kibana can be used for visualization, however we've since designed a dashboard using Plotly.

https://www.elastic.co/guide/en/kibana/current/setup.html

In the Kibana installation directory, do:

./bin/kibana

Access at:
http://localhost:5601
