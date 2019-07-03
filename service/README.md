# Benchmark CI

## Benchmark Service

The benchmark web server is a google cloud instance that is always running.

It hosts:
* Elasticsearch - persists benchmark data and web server metadata, listens on port 9200
* Web Server - node.js web server that listens for Github events and hosts the web dashboard

### Launching Benchmark Service
To be able to launch the benchmark service you need to have
1. `gcloud`
2. Authenticated with `gcloud`: `gcloud auth login`
3. Have the `iam.ServiceAccountUser` role assigned to you in GCP

Then:
* `./launch_service.sh <optional instance name>` launches the benchmark service.


## Benchmark Executor Service

The executor server is a dynamically spawned GCP instance that actually executes the benchmark on
Grakn. It is spawned and deleted transparently by the `service` and so does not need
to be interacted with directly.


## Starting the ElasticSearch Server

```shell
$ sudo sysctl -w vm.max_map_count=262144
$ cd /home/ubuntu/elastic/elasticsearch-6.3.2/
$ ./bin/elasticsearch  -E path.logs=data/elasticsearch/logs -E path.data=data/elasticsearch/data -E network.host=0.0.0.0 &
```