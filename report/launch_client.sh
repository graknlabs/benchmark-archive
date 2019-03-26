#!/bin/bash

if [ $# -ne 1 ]
then
    echo "No arguments supplied"
    echo "Usage ./launch-client.sh <grakn server google cloud instance name>"
    exit 1;
fi

GRAKN_URI=$1

git clone https://github.com/graknlabs/benchmark.git
cd benchmark

# TODO wait until grakn gRPC port is available
sleep 30


# TODO use config files and run this via a built distribution ZIP rather than a hardcoded config path
bazel run //report:report-generator-binary -- --config=/home/ubuntu/benchmark/common/scenario/road_network/road_config_read.yml --execution-name "road-read" --grakn-uri $GRAKN_URI:48555 --keyspace road_read
bazel run //report:report-generator-binary -- --config=/home/ubuntu/benchmark/common/scenario/road_network/road_config_write.yml --execution-name "road-write" --grakn-uri $GRAKN_URI:48555 --keyspace road_write

bazel run //report:report-generator-binary -- --config=/home/ubuntu/benchmark/common/scenario/complex/config_read.yml --execution-name "complex-read" --grakn-uri $GRAKN_URI:48555 --keyspace complex_read
bazel run //report:report-generator-binary -- --config=/home/ubuntu/benchmark/common/scenario/complex/config_write.yml --execution-name "complex-write" --grakn-uri $GRAKN_URI:48555 --keyspace complex_write


# TODO aggregate JSON files produced
