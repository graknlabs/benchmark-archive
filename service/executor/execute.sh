#!/bin/bash

report_failure() {
  curl --header "Content-Type: application/json" \
    --request POST \
    --data "{\"executionId\":\"$EXECUTION_ID\" }" \
    http://$SERVICE_IP:80/execution/failed

  exit 1
}
 # catch any error - report failure to Service and exit the script
trap report_failure ERR

if [ $# -ne 5 ]
then
    echo "Require 5 arguments"
    echo "Usage: ./execute.sh <repo https url> <commit id> <benchmark service IP> <this instance name> <execution id>"
    report_failure # exit with report of failure
fi

GRAKN_REPOSITORY_URL=$1
COMMIT=$2
SERVICE_IP=$3
INSTANCE_NAME=$4
EXECUTION_ID=$5

cd ~

# --- get grakn ---
git clone $GRAKN_REPOSITORY_URL

# build grakn
cd grakn
git checkout $COMMIT 

bazel build //:assemble-linux-targz

# unzip grakn
cd bazel-genfiles
tar -xf grakn-core-all-linux.tar.gz

# start grakn
cd grakn-core-all-linux
./grakn server start --benchmark

# reset dir
cd ~

# --- clone benchmark ---
git clone https://github.com/graknlabs/benchmark.git

# build benchmark
cd benchmark

bazel build //:profiler-distribution

# unzip benchmark
cd bazel-genfiles
unzip profiler.zip

cd profiler

# --- run zipkin ---
tmux new-session -d -s zipkin "STORAGE_TYPE=elasticsearch ES_HOSTS=http://$SERVICE_IP:9200 ES_INDEX=benchmark java -jar external-dependencies/zipkin.jar"


# notify benchmark service about start
echo "Notifying Benchmark Service /start"
curl --header "Content-Type: application/json" \
    --request POST \
    --data "{\"executionId\":\"$EXECUTION_ID\" }" \
    http://$SERVICE_IP:80/execution/start

# -- write queries --
./benchmark --config ./scenario/road_network/road_config_write.yml --execution-name "$EXECUTION_ID" --elastic-uri $SERVICE_IP:9200
./benchmark --config ./scenario/complex/config_write.yml --execution-name "$EXECUTION_ID" --elastic-uri $SERVICE_IP:9200

# -- read queries --
./benchmark --config ./scenario/road_network/road_config_read.yml                --execution-name "$EXECUTION_ID" --elastic-uri $SERVICE_IP:9200 --keyspace road_network_read
./benchmark --config ./scenario/financial_transactions/financial_config_read.yml --execution-name "$EXECUTION_ID" --elastic-uri $SERVICE_IP:9200
./benchmark --config ./scenario/social_network/social_network_config_read.yml    --execution-name "$EXECUTION_ID" --elastic-uri $SERVICE_IP:9200
./benchmark --config ./scenario/biochemical_network/biochemical_config_read.yml  --execution-name "$EXECUTION_ID" --elastic-uri $SERVICE_IP:9200
./benchmark --config ./scenario/complex/config_read.yml                          --execution-name "$EXECUTION_ID" --elastic-uri $SERVICE_IP:9200 --keyspace generic_uniform_network_read
./benchmark --config ./scenario/reasoning/config_read.yml                        --execution-name "$EXECUTION_ID" --elastic-uri $SERVICE_IP:9200 --keyspace reasoner --load-schema --static-data-import
./benchmark --config ./scenario/rule_scaling/config_read.yml                     --execution-name "$EXECUTION_ID" --elastic-uri $SERVICE_IP:9200 --keyspace rule_scaling --load-schema --static-data-import
# TODO report log files

# Call completed hook on the service
curl --header "Content-Type: application/json" \
    --request POST \
    --data "{\"executionId\":\"$EXECUTION_ID\", \"vmName\": \"$INSTANCE_NAME\"}" \
    http://$SERVICE_IP:80/execution/completed
