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
    name = "benchmark-runner-binary",
    main_class = "grakn.benchmark.runner.GraknBenchmark",
    runtime_deps = ["//runner:benchmark-runner"],
    visibility = ["//visibility:public"],
    classpath_resources = ["logback"]
 )

java_library(
    name = "benchmark-runner",
    srcs = glob(["src/**/*.java"]),
    deps = [
        "//dependencies/maven/artifacts/grakn/core:client",
        "//dependencies/maven/artifacts/grakn/core:grakn-graql",
        "//dependencies/maven/artifacts/grakn/core:util",

        "//dependencies/maven/artifacts/com/fasterxml/jackson/core:jackson-core",
        "//dependencies/maven/artifacts/com/fasterxml/jackson/core:jackson-databind",
        "//dependencies/maven/artifacts/com/fasterxml/jackson/dataformat:jackson-dataformat-yaml",

        "//dependencies/maven/artifacts/io/zipkin/brave:brave",

        "//dependencies/maven/artifacts/org/slf4j:slf4j-api",
        "//dependencies/maven/artifacts/ch/qos/logback:logback-classic",

        "//dependencies/maven/artifacts/org/apache/ignite:ignite-core",
        "//dependencies/maven/artifacts/org/apache/ignite:ignite-indexing",
        "//dependencies/maven/artifacts/org/apache/ignite:ignite-slf4j",

        "//dependencies/maven/artifacts/org/elasticsearch/client:elasticsearch-rest-client",
        "//dependencies/maven/artifacts/org/apache/httpcomponents:httpcore",
        "//dependencies/maven/artifacts/org/apache/httpcomponents:httpclient",

        "//dependencies/maven/artifacts/commons-cli",
        "//dependencies/maven/artifacts/org/apache/commons:commons-math3",

        "//dependencies/maven/artifacts/com/google/guava:guava"
    ],
    runtime_deps = [
        "//dependencies/maven/artifacts/io/grpc:grpc-netty-shaded"
    ],
    visibility = ["//visibility:public"]
)

filegroup(
    name = "logback",
    srcs = ["src/logback.xml"],
    visibility = ["//visibility:public"]
)