#!/bin/bash

report_failure() {
  cp ~/log.txt ~/report.json
  exit 1
}
 # catch any error
trap report_failure ERR



if [ $# -ne 1 ]
then
    echo "No arguments supplied"
    echo "Usage ./launch-client.sh <grakn server google cloud instance name>"
    exit 1;
fi


GRAKN_URI=$1

git clone https://github.com/flyingsilverfin/benchmark.git
cd benchmark
git checkout concurrent-report-clients

bazel build //:report-producer-distribution
cd bazel-genfiles
unzip report-producer.zip
cd report-producer


# Wait until server machine is up and running
echo "Waiting for $GRAKN_URI to be up and running..."

trap - ERR # disable trap temporarily
RET=1
while [ $RET -ne 0 ]; do
    sleep 1;
    curl $GRAKN_URI:48555
    RET=$?; # collect return code
done
trap report_failure ERR


./report_producer --config=scenario/road_network/road_config_read.yml --execution-name "road-read" --grakn-uri $GRAKN_URI:48555 --keyspace road_read
./report_producer --config=scenario/road_network/road_config_read_c4.yml --execution-name "road-read-c4" --grakn-uri $GRAKN_URI:48555 --keyspace road_read_c4
./report_producer --config=scenario/road_network/road_config_read_c8.yml --execution-name "road-read-c8" --grakn-uri $GRAKN_URI:48555 --keyspace road_read_c8
./report_producer --config=scenario/road_network/road_config_write.yml --execution-name "road-write" --grakn-uri $GRAKN_URI:48555 --keyspace road_write
./report_producer --config=scenario/road_network/road_config_write_c4.yml --execution-name "road-write-c4" --grakn-uri $GRAKN_URI:48555 --keyspace road_write_c4
./report_producer --config=scenario/road_network/road_config_write_c8.yml --execution-name "road-write-c8" --grakn-uri $GRAKN_URI:48555 --keyspace road_write_c8

# TODO re-enable complex reads when statistics are available so queries terminate in reasonable time
#./report_producer --config=scenario/complex/config_read.yml --execution-name "complex-read" --grakn-uri $GRAKN_URI:48555 --keyspace complex_read
#./report_producer --config=scenario/complex/config_read_c4.yml --execution-name "complex-read-c4" --grakn-uri $GRAKN_URI:48555 --keyspace complex_read_c4
#./report_producer --config=scenario/complex/config_read_c8.yml --execution-name "complex-read-c8" --grakn-uri $GRAKN_URI:48555 --keyspace complex_read_c8
./report_producer --config=scenario/complex/config_write.yml --execution-name "complex-write" --grakn-uri $GRAKN_URI:48555 --keyspace complex_write
./report_producer --config=scenario/complex/config_write_c4.yml --execution-name "complex-write-c4" --grakn-uri $GRAKN_URI:48555 --keyspace complex_write_c4
./report_producer --config=scenario/complex/config_write_c8.yml --execution-name "complex-write-c8" --grakn-uri $GRAKN_URI:48555 --keyspace complex_write_c8


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