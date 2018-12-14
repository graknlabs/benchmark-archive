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
    name = "metric",
    srcs = glob(["*.java"]),
    deps = [
        "//dependencies/maven/artifacts/grakn/core:client",
        "//dependencies/maven/artifacts/grakn/core:grakn-graql",
        "//dependencies/maven/artifacts/grakn/core:util",

        "//dependencies/maven/artifacts/commons-cli",
        "//dependencies/maven/artifacts/org/apache/commons:commons-math3",

        "//dependencies/maven/artifacts/org/nd4j:nd4j-api"
    ],
    runtime_deps = [
        "//dependencies/maven/artifacts/io/grpc:grpc-netty-shaded"
    ],
    resources = [
        "//metric:logback.xml"
    ]
)

java_binary(
    name = "metric-binary",
    main_class = "grakn.benchmark.metric.MetricMeasurement",
    runtime_deps = [":metric"],
    visibility = ["//visibility:public"]
)


java_test(
    name = "metric-end-to-end",
    test_class = "grakn.benchmark.metric.test.MetricMeasurementEndToEnd",
    srcs = ["test/MetricMeasurementEndToEnd.java"],
    deps = [
        "//metric:metric",
        "//dependencies/maven/artifacts/junit:junit",
        "//dependencies/maven/artifacts/org/mockito:mockito-core",
        "//dependencies/maven/artifacts/org/hamcrest:hamcrest-library"
    ]
)

java_test(
    name = "assortativity-test",
    test_class = "grakn.benchmark.metric.test.AssortativityTest",
    srcs = ["test/AssortativityTest.java"],
    deps = [
        "//metric:metric",
        "//dependencies/maven/artifacts/org/apache/commons:commons-math3",
        "//dependencies/maven/artifacts/junit:junit",
        "//dependencies/maven/artifacts/org/mockito:mockito-core",
        "//dependencies/maven/artifacts/org/nd4j:nd4j-api"
    ]
)
