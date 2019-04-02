#
#  GRAKN.AI - THE KNOWLEDGE GRAPH
#  Copyright (C) 2019 Grakn Labs Ltd
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

exports_files(["report_producer"], visibility = ["//visibility:public"])
load("@graknlabs_bazel_distribution//common:rules.bzl", "java_deps")

java_library(
    name = "report-producer",
    srcs = glob(["*.java", "container/*.java"]),
    deps = [
        "//generator:data-generator",
        "//common/configuration:benchmark-configuration",
        "//common/exception:benchmark-exception",

        "@graknlabs_client_java//:client-java",
        "@graknlabs_grakn_core//api:api",
        "@graknlabs_graql//java:graql",
        "@graknlabs_grakn_core//concept:concept",

        "//dependencies/maven/artifacts/org/apache/ignite:ignite-core",

        "//dependencies/maven/artifacts/commons-cli",
        "//dependencies/maven/artifacts/org/slf4j:slf4j-api",

        "//dependencies/maven/artifacts/com/fasterxml/jackson/core:jackson-core",
        "//dependencies/maven/artifacts/com/fasterxml/jackson/core:jackson-databind",
    ],
    visibility = ["//visibility:public"]
)

java_binary(
    name = "report-producer-binary",
    main_class = "grakn.benchmark.report.ReportProducer",
    runtime_deps = [
        "//report/producer:report-producer",
    ],
    classpath_resources = [":logback"],
    visibility = ["//visibility:public"]
)

java_deps(
    name = "report-deps",
    target = "//report/producer:report-producer-binary",
    java_deps_root = "lib/",
    version_file = "//:VERSION",
    visibility = ["//visibility:public"]
)

filegroup(
    name = "logback",
    srcs = ["//report/producer:logback.xml"],
    visibility = ["//visibility:public"]
)