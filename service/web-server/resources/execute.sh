#!/bin/bash

report_failure() {
  # mark execution as failed
  echo "Notifying Benchmark Service /failed"
  curl --header "Content-Type: application/json" \
    --request POST \
    --data "{\"execution\": { \"id\": \"$EXECUTION_ID\", \"vmName\": \"$VM_NAME\" } }" \
    --insecure \
    $WEB_URI/execution/failed

  exit 1
}
# catch any error - report failure to Service and exit the script
trap report_failure ERR

if [ $# -ne 6 ]
then
    echo "Require 5 arguments"
    echo "Usage: ./execute.sh <repo https url> <commit id> <benchmark service IP> <this instance name> <execution id>"
    report_failure # exit with report of failure
fi

GRAKN_REPOSITORY_URL=$1
COMMIT=$2
ES_URI=$3
WEB_URI=$4
VM_NAME=$5
EXECUTION_ID=$6

# clone, build and unzip grakn
cd ~
git clone $GRAKN_REPOSITORY_URL
cd grakn
git checkout $COMMIT
bazel build //:assemble-linux-targz
cd bazel-genfiles
tar -xf grakn-core-all-linux.tar.gz

# start grakn
cd grakn-core-all-linux
./grakn server start --benchmark

# clone, build and unzip grakn
cd ~
git clone https://github.com/graknlabs/benchmark.git
cd benchmark
bazel build //:profiler-distribution
cd bazel-genfiles
unzip -o profiler.zip

# run zipkin
cd profiler
tmux new-session -d -s zipkin "STORAGE_TYPE=elasticsearch ES_HOSTS=http://$ES_URI ES_INDEX=benchmark java -jar external-dependencies/zipkin.jar"


# mark execution as running
echo "Notifying Benchmark Service /start"
curl --header "Content-Type: application/json" \
    --request POST \
    --data "{\"execution\": { \"id\": \"$EXECUTION_ID\" } }" \
    --insecure \
    $WEB_URI/execution/start

# benchmark write queries
./benchmark --config ./scenario/road_network/road_config_write.yml --execution-name "$EXECUTION_ID" --elastic-uri $ES_URI
./benchmark --config ./scenario/complex/config_write.yml --execution-name "$EXECUTION_ID" --elastic-uri $ES_URI

# benchmark read queries
./benchmark --config ./scenario/road_network/road_config_read.yml                --execution-name "$EXECUTION_ID" --elastic-uri $ES_URI --keyspace road_network_read
./benchmark --config ./scenario/biochemical_network/biochemical_config_read.yml  --execution-name "$EXECUTION_ID" --elastic-uri $ES_URI
./benchmark --config ./scenario/complex/config_read.yml                          --execution-name "$EXECUTION_ID" --elastic-uri $ES_URI --keyspace generic_uniform_network_read
./benchmark --config ./scenario/reasoning/config_read.yml                        --execution-name "$EXECUTION_ID" --elastic-uri $ES_URI --keyspace reasoner --load-schema --static-data-import
./benchmark --config ./scenario/rule_scaling/config_read.yml                     --execution-name "$EXECUTION_ID" --elastic-uri $ES_URI --keyspace rule_scaling --load-schema --static-data-import
./benchmark --config ./scenario/schema/data_definition_config.yml                --execution-name "$EXECUTION_ID" --elastic-uri $ES_URI --keyspace schema --load-schema --no-data-generation
./benchmark --config ./scenario/attribute/attribute_read_config.yml              --execution-name "$EXECUTION_ID" --elastic-uri $ES_URI --keyspace attribute

# TODO report log files

# mark execution as completed
curl --header "Content-Type: application/json" \
    --request POST \
    --data "{\"execution\": { \"id\": \"$EXECUTION_ID\" } }" \
    --insecure \
    $WEB_URI/execution/completed