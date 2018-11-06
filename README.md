# Benchmarking

To get started, Grakn, Ignite, Elasticsearch and Zipkin need to be running.

## Elasticsearch
https://www.elastic.co/guide/en/elasticsearch/reference/6.3/zip-targz.html

In the elasticsearch installation directory, do:
```
./bin/elasticsearch -E path.logs=[GRAKN_PATH]/grakn-benchmark/data/logs/elasticsearch/ -E path.data=[GRAKN_PATH]/grakn-benchmark/data/data/elasticsearch/
```
Here, `[GRAKN_PATH]` is the path to your `grakn` directory.

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

We have a dashboard that reads ElasticSearch and creates graphs via Dash and Plotly.

To get up and running, you need pipenv and python >=3.6.0

1. `pipenv install` (installs package dependencies for the dashboard)
2. `pipenv shell` (you may need to modify the `python_version = "3.6"` if the python version you have is newer/not quite the same. Alternatively runner your python versions with `pyenv`.
3. in the `dashboard/` directory, run `python dashboard.py`
4. Navigate to `http:localhost:8050` to see the dashboard

The box plots are individually clickable to drill down, bar charts (default if only 1 repetition is being displayed) cycle through drill downs on each click.

## Executing Benchmarks and Generating Data

We define YAML config files to execute under `grakn-benchmark/src/main/resources/`

The entry point to rebuild, generate, and name executions of config files is `run.py`

Basic usage:
`run.py --config grakn-benchmark/src/main/resources/societal_config_1.yml --execution-name query-plan-mod-1 --keyspace benchmark --ignite-dir /Users/user/Documents/benchmarking-reqs/apache-ignite-fabric-2.6.0-bin/`

Notes:
* Naming the execution not required, default name is always prepended with current Date and `name` tag in the YAML file
* Keyspace is not required, defaults to `name` in the YAML file
* Because ignite is not currently embedded, we need the directory of the ignite bin folder to search for `jar` files which contain ignite drivers

Further examples:

Stop and re-unpack Grakn server, then run
`run.py --unpack-tar --config grakn-benchmark/src/main/resources/societal_config_1.yml`

Rebuild Grakn server, stop and remove the old one, untar, then run
`run.py --build-grakn --config grakn-benchmark/src/main/resources/societal_config_1.yml`

Rebuild Benchmarking and its dependencies and execute
`run.py --build-benchmark--alldeps --config grakn-benchmark/src/main/resources/societal_config_1.yml`

** TODO revisit the run.py to see if we need it at all, especially with Bazel (no more need to collect classpath) **


### Adding new spans to measure code segments

* On the server (or in the Java client), we can obtain the current Tracer with:
```
Tracer tracer = Tracing.currentTracer(); 
```

Then add a child span
```

        Tracer tracer = Tracing.currentTracer();
        ScopedSpan childSpan = null;
        if (tracer != null) {
            Span s = tracer.currentSpan();
            if (s != null) {
                childSpan = tracer.startScopedSpanWithParent("planForConjunction", s.context());
            } else {
                childSpan = tracer.startScopedSpan("planForConjunction");
            }
        }
```

and finish with 
```
        if (tracer != null && childSpan != null) {
            childSpan.finish();
        }

```




## Kibana
Kibana can be used for visualization, however we've since designed a dashboard using Plotly.

https://www.elastic.co/guide/en/kibana/current/setup.html

In the Kibana installation directory, do:

./bin/kibana

Access at:
http://localhost:5601
