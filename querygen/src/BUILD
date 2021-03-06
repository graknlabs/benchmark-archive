#
# Copyright (C) 2020 Grakn Labs
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

load("@graknlabs_build_tools//checkstyle:rules.bzl", "checkstyle_test")

java_library(
    name = "query-sampler-lib",
    srcs = glob(["*.java", "subsampling/*.java", "util/*.java"]),
    deps = [
        "@graknlabs_client_java//:client-java",
        "@graknlabs_graql//java:graql",

        # together these can be used to create clients with tracing enabled
        "//dependencies/maven/artifacts/org/slf4j:slf4j-api",
        "//dependencies/maven/artifacts/commons-cli",
    ],
    runtime_deps = [
        "//dependencies/maven/artifacts/io/grpc:grpc-netty-shaded",
        "//dependencies/maven/artifacts/ch/qos/logback:logback-classic",
    ],
    visibility = ["//visibility:public"]
)

java_binary(
    name = "query-sampler",
    main_class = "grakn.benchmark.querygen.QuerySampler",
    runtime_deps = [
        ":query-sampler-lib"
    ],
    classpath_resources = [":logback"],
    visibility = ["//visibility:public"]
)

filegroup(
    name = "logback",
    srcs = ["//querygen/src:logback.xml"],
    visibility = ["//visibility:public"]
)

checkstyle_test(
    name = "checkstyle",
    targets = [
        ":query-sampler-lib",
    ],
    license_type = "agpl"
)
