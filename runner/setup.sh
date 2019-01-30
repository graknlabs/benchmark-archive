#!/usr/bin/env bash
#
# GRAKN.AI - THE KNOWLEDGE GRAPH
# Copyright (C) 2018 Grakn Labs Ltd
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as
# published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <https://www.gnu.org/licenses/>.
#

# NOTE: use set -ex for debugging
set -e

trap on_receiving_ctrl_c INT

# Benchmark global variables
JAVA_BIN=java
[[ $(readlink $0) ]] && path=$(readlink $0) || path=$0
WORKING_DIR=$(cd "$(dirname "${path}")" && pwd -P)
BENCHMARK_RUNNER_EXTERNAL_DEPS_DIR=.

# ================================================
# common helper functions
# ================================================
on_receiving_ctrl_c() {
  echo "Benchmark Runner Finshised. Stopping Elasticsearch..."
  kill $elasticsearch_pid
  echo "Stopping Zipkin..."
  kill $zipkin_pid
}

exit_if_java_not_found() {
  which "${JAVA_BIN}" > /dev/null
  exit_code=$?

  if [[ $exit_code -ne 0 ]]; then
    echo "Java is not installed on this machine. Benchmark needs Java 1.8 in order to run."
    exit 1
  fi
}

# =============================================
# main routine
# =============================================
exit_code=0

pushd "$WORKING_DIR" > /dev/null
exit_if_java_not_found

if [[ ! -d $BENCHMARK_RUNNER_EXTERNAL_DEPS_DIR/elasticsearch-6.3.2 ]]; then
  echo "Unzipping Elasticsearch..."
  unzip -q $BENCHMARK_RUNNER_EXTERNAL_DEPS_DIR/elasticsearch.zip -d $BENCHMARK_RUNNER_EXTERNAL_DEPS_DIR
fi
echo -n "Starting Elasticsearch"
$BENCHMARK_RUNNER_EXTERNAL_DEPS_DIR/elasticsearch-6.3.2/bin/elasticsearch -E path.logs=data/logs/elasticsearch/ -E path.data=data/data/elasticsearch/ &
elasticsearch_pid=$!
until $(curl --output /dev/null --silent --head --fail localhost:9200); do
  echo -n "."
  sleep 1
done
echo

echo -n "Starting Zipkin"
ES_HOSTS=http://localhost:9200 env ES_INDEX="benchmark" STORAGE_TYPE=elasticsearch java -jar $BENCHMARK_RUNNER_EXTERNAL_DEPS_DIR/zipkin.jar &
zipkin_pid=$!
until $(curl --output /dev/null --silent --head --fail localhost:9411); do
  echo -n "."
  sleep 1
done
echo

exit_code=$?

popd > /dev/null

exit $exit_code