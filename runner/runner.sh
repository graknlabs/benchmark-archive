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

# Benchmark global variables
JAVA_BIN=java
[[ $(readlink $0) ]] && path=$(readlink $0) || path=$0
WORKING_DIR=$(cd "$(dirname "${path}")" && pwd -P)
BENCHMARK_RUNNER_EXTERNAL_DEPS_DIR=external-dependencies
BENCHMARK_RUNNER_SERVICE_LIB_CP="services/lib/*"
BENCHMARK_LOGBACK="services"

exit_if_java_not_found(                                     ) {
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


echo "Starting Benchmark Runner"
CLASSPATH="${WORKING_DIR}/${BENCHMARK_RUNNER_SERVICE_LIB_CP}:${WORKING_DIR}/${BENCHMARK_LOGBACK}"
java -cp "${CLASSPATH}" -Dworking.dir="${WORKING_DIR}" -Xms512m -Xmx512m grakn.benchmark.runner.GraknBenchmark $@

exit_code=$?

popd > /dev/null

exit $exit_code