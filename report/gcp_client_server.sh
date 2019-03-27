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


python -c '
import json
import glob

json_files = glob.glob("*.json")

merged_json = {
    'metadata' : [],
    'queryExecutionData': {}
}

for file in json_files:
    j = json.load(open(file))
    merged_json['metadata'].append(j['metdata'])

    query_execution_data = j['queryExecutionData']

    merged_execution_data = merged_json['queryExecutionData']

    for key in query_execution_data:
        data = query_execution_data[key]
        if key not in merged_execution_data:
            merged_execution_data[key] = []
        merged_execution_data[key].extend(data)

json.dump(merged_json, open('report.json', 'w'), indent=4)
'

cp report.json ~/report.json