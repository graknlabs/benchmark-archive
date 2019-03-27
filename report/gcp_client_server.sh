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

bazel build //:report-generator-distribution
cd bazel-genfiles
unzip report-generator.zip
cd report-generator


# TODO wait until grakn gRPC port is available
sleep 30


# TODO use config files and run this via a built distribution ZIP rather than a hardcoded config path
./report_generator --config=scenario/road_network/road_config_read.yml --execution-name "road-read" --grakn-uri $GRAKN_URI:48555 --keyspace road_read
./report_generator --config=scenario/road_network/road_config_write.yml --execution-name "road-write" --grakn-uri $GRAKN_URI:48555 --keyspace road_write

./report_generator --config=scenario/complex/config_read.yml --execution-name "complex-read" --grakn-uri $GRAKN_URI:48555 --keyspace complex_read
./report_generator --config=scenario/complex/config_write.yml --execution-name "complex-write" --grakn-uri $GRAKN_URI:48555 --keyspace complex_write


# TODO aggregate JSON files produced
