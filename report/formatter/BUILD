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

exports_files(["report_formatter"], visibility = ["//visibility:public"])
load("@graknlabs_bazel_distribution//common:rules.bzl", "java_deps")

java_library(
    name = "report-formatter",
    srcs = glob(["*.java"]),
    deps = [
        "//dependencies/maven/artifacts/commons-cli",
        "//dependencies/maven/artifacts/org/slf4j:slf4j-api",
        "//dependencies/maven/artifacts/org/sharegov:mjson"
    ],
    visibility = ["//visibility:public"]
)

java_binary(
    name = "report-formatter-binary",
    main_class = "grakn.benchmark.report.formatter.ReportFormatter",
    runtime_deps = [
        ":report-formatter",
    ],
    classpath_resources = [":logback"],
    visibility = ["//visibility:public"]
)

filegroup(
    name = "logback",
    srcs = ["//report/formatter:logback.xml"],
    visibility = ["//visibility:public"]
)