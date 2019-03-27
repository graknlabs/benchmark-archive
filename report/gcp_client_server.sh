#!/bin/bash

if [ $# -ne 1 ]
then
    echo "No arguments supplied"
    echo "Usage ./launch-client.sh <grakn server google cloud instance name>"
    exit 1;
fi

GRAKN_URI=$1

# TODO replace this with `graknlabs` url
git clone https://github.com/flyingsilverfin/benchmark.git
cd benchmark
git checkout report-generator-scripts

bazel build //:report-generator-distribution
cd bazel-genfiles
unzip report-generator.zip
cd report-generator


# TODO wait until grakn gRPC port is available
sleep 30


./report_generator --config=scenario/road_network/road_config_read.yml --execution-name "road-read" --grakn-uri $GRAKN_URI:48555 --keyspace road_read
./report_generator --config=scenario/road_network/road_config_write.yml --execution-name "road-write" --grakn-uri $GRAKN_URI:48555 --keyspace road_write

./report_generator --config=scenario/complex/config_read.yml --execution-name "complex-read" --grakn-uri $GRAKN_URI:48555 --keyspace complex_read
./report_generator --config=scenario/complex/config_write.yml --execution-name "complex-write" --grakn-uri $GRAKN_URI:48555 --keyspace complex_write


# merge the JSON files into a single file and write to a single output
python -c '
import json
import glob

json_files = glob.glob("*.json")
output = [json.load(open(file)) for file in json_files]
json.dump(output, open("report.json", "w"), indent=4)
'

# copy output to a known location that is polled on from outside
cp report.json ~/report.json