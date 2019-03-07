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
    name = "benchmark-profiler",
    srcs = glob(["*.java", "util/*.java"]),
    deps = [
        "@graknlabs_client_java//:client-java",
        "@graknlabs_grakn_core//api:api",
        "@graknlabs_grakn_core//server:server",
        "@graknlabs_grakn_core//common:common",
        "@graknlabs_graql//java:graql",
        "@graknlabs_grakn_core//concept:concept",

        "//lib:lib",

        # TODO should this be via grakn-core's deps?
        "//dependencies/maven/artifacts/io/grpc:grpc-core",

        "//dependencies/maven/artifacts/com/fasterxml/jackson/core:jackson-core",
        "//dependencies/maven/artifacts/com/fasterxml/jackson/core:jackson-databind",
        "//dependencies/maven/artifacts/com/fasterxml/jackson/dataformat:jackson-dataformat-yaml",

        "//dependencies/maven/artifacts/io/zipkin/brave:brave",

        "//dependencies/maven/artifacts/org/slf4j:slf4j-api",
        "//dependencies/maven/artifacts/org/elasticsearch/client:elasticsearch-rest-client",
        "//dependencies/maven/artifacts/org/apache/httpcomponents:httpcore",
        "//dependencies/maven/artifacts/org/apache/httpcomponents:httpclient",

        "//dependencies/maven/artifacts/commons-cli",

        "//dependencies/maven/artifacts/org/apache/ignite:ignite-core",

        "//profiler/src/generator:data-generator"
    ],
    runtime_deps = [
        "//dependencies/maven/artifacts/io/grpc:grpc-netty-shaded",
        "//dependencies/maven/artifacts/ch/qos/logback:logback-classic",
    ],
    visibility = ["//visibility:public"]
)

filegroup(
    name = "logback",
    srcs = ["//profiler/src:logback.xml"],
    visibility = ["//visibility:public"]
)