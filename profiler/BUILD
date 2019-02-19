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

exports_files(glob(["conf/**/*"]) + ["benchmark"] + ["setup.sh"], visibility = ["//visibility:public"])

java_binary(
    name = "benchmark-profiler-binary",
    runtime_deps = [
        ":benchmark-profiler"
    ],
    main_class = "grakn.benchmark.profiler.GraknBenchmark",
    classpath_resources = [":logback"],
    visibility = ["//visibility:public"]
)

java_library(
    name = "benchmark-profiler",
    srcs = glob(["src/*.java", "src/util/*.java"]),
    deps = [
        "//dependencies/maven/artifacts/grakn/core:client",
        "//dependencies/maven/artifacts/grakn/core:server",
        "//dependencies/maven/artifacts/grakn/core:graql",

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
    srcs = ["src/logback.xml"],
    visibility = ["//visibility:public"]
)