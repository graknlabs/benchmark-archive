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

# ----- Nd4j platform specific binaries -----

config_setting(
    name = "osx_build",
    constraint_values = [
   		"@bazel_tools//platforms:osx",
        "@bazel_tools//platforms:x86_64",
    ]
)

config_setting(
    name = "linux_build",
    constraint_values = [
   		"@bazel_tools//platforms:linux",
        "@bazel_tools//platforms:x86_64",
    ]
)

java_library(
	name = "nd4j-native-platform-independent",
	exports = select({
		":linux_build": ["//dependencies/maven/artifacts/org/nd4j:nd4j-native-jar-linux-x86-64"],
		":osx_build": ["//dependencies/maven/artifacts/org/nd4j:nd4j-native-jar-macosx-x86-64"]
	})
)


java_library(
    name = "metric",
    srcs = glob(["*.java"]),
    deps = [
        "//dependencies/maven/artifacts/grakn/core:client",
        "//dependencies/maven/artifacts/grakn/core:grakn-graql",
        "//dependencies/maven/artifacts/grakn/core:util",

        "//dependencies/maven/artifacts/commons-cli",
        "//dependencies/maven/artifacts/org/apache/commons:commons-math3",

         "//dependencies/maven/artifacts/org/nd4j:nd4j-native",
         "//dependencies/maven/artifacts/org/nd4j:nd4j-api",
         "//dependencies/maven/artifacts/org/nd4j:nd4j-native-api",
         "//dependencies/maven/artifacts/org/nd4j:nd4j-buffer",
         ":nd4j-native-platform-independent"
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
     name = "assortativity-it",
     test_class = "grakn.benchmark.metric.test.AssortativityIT",
     srcs = ["test/AssortativityIT.java"],
     deps = [
         "//metric:metric",
         "//dependencies/maven/artifacts/junit:junit",

        "//dependencies/maven/artifacts/grakn/core:client",
        "//dependencies/maven/artifacts/grakn/core:grakn-graql",
        "//dependencies/maven/artifacts/grakn/core:util"
     ],
     data = [":test/binaryGraph.csv", ":test/unaryBinaryGraph.csv"]
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

          "//dependencies/maven/artifacts/org/nd4j:nd4j-native",
          "//dependencies/maven/artifacts/org/nd4j:nd4j-api",
          "//dependencies/maven/artifacts/org/nd4j:nd4j-native-api",
          ":nd4j-native-platform-independent"
     ]
)


java_test(
     name = "degree-distribution-it",
     test_class = "grakn.benchmark.metric.test.DegreeDistributionIT",
     srcs = ["test/DegreeDistributionIT.java"],
     deps = [
         "//metric:metric",
         "//dependencies/maven/artifacts/junit:junit",

        "//dependencies/maven/artifacts/grakn/core:client",
        "//dependencies/maven/artifacts/grakn/core:grakn-graql",
        "//dependencies/maven/artifacts/grakn/core:util"
     ],
     data = [":test/binaryGraph.csv", ":test/unaryBinaryGraph.csv"]
 )

java_test(
     name = "degree-distribution-test",
     test_class = "grakn.benchmark.metric.test.DegreeDistributionTest",
     srcs = ["test/DegreeDistributionTest.java"],
     deps = [
         "//metric:metric",
         "//dependencies/maven/artifacts/junit:junit",
         "//dependencies/maven/artifacts/org/mockito:mockito-core",
     ]
 )

java_test(
    name = "global-transitivity-it",
    test_class = "grakn.benchmark.metric.test.GlobalTransitivityIT",
    srcs = ["test/GlobalTransitivityIT.java"],
    deps = [
        "//metric:metric",
        "//dependencies/maven/artifacts/junit:junit",

        "//dependencies/maven/artifacts/grakn/core:client",
        "//dependencies/maven/artifacts/grakn/core:grakn-graql",
        "//dependencies/maven/artifacts/grakn/core:util"
    ],
    data = [":test/binaryGraph.csv", ":test/unaryBinaryGraph.csv"]
)

java_test(
    name = "global-transitivity-test",
    test_class = "grakn.benchmark.metric.test.GlobalTransitivityTest",
    srcs = ["test/GlobalTransitivityTest.java"],
    deps = [
        "//metric:metric",
        "//dependencies/maven/artifacts/junit:junit",
        "//dependencies/maven/artifacts/org/mockito:mockito-core",
        "//dependencies/maven/artifacts/org/apache/commons:commons-math3",
    ]
)
