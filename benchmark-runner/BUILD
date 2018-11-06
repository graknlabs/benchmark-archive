#
#  GRAKN.AI - THE KNOWLEDGE GRAPH
#  Copyright (C) 2018 Grakn Labs Ltd
#
#  This program is free software: you can redistribute it and/or modify
#  it under the terms of the GNU Affero General Public License as
#  published by the Free Software Foundation, either version 3 of the
#  License, or (at your option) any later version.
#
#  This program is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU Affero General Public License for more details.
#
#  You should have received a copy of the GNU Affero General Public License
#  along with this program.  If not, see <https://www.gnu.org/licenses/>.
#


java_library(
    name = "benchmark_runner",
    srcs = glob(["src/**/*.java"]),
    deps = [
        "@grakn_client_java//jar",
        "@slf4j//jar",
        "@ignite_core//jar",
        "@ignite_indexing//jar",
        "@mockito_core//jar",
        "@jackson_yaml//jar",
        "@elasticsearch_rest_client//jar",
        "@apache_httpcore//jar",
        "@apache_httpclient//jar",
        "@apache_commons_cli//jar",
        "@apache_commons_math3//jar",
        "@brave//jar"
    ]
)

java_binary(
    name = "benchmark_runner_binary",
    main_class = "runner.BenchmarkRunner",
    runtime_deps = ["//benchmark-runner:benchmark_runner"],
    visibility = ["//visibility:public"]
)



# TODO
# java_test()


