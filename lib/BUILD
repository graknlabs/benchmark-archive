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
    name = "server-instrumentation",
    srcs = glob(["instrumentation/ServerTracing.java", "util/*.java"]),
    deps = [
        "@graknlabs_grakn_core//protocol:protocol-java",
        "//dependencies/maven/artifacts/io/zipkin/brave:brave",
        "//dependencies/maven/artifacts/io/zipkin/zipkin2:zipkin",
        "//dependencies/maven/artifacts/io/zipkin/reporter2:zipkin-reporter",
        "//dependencies/maven/artifacts/io/zipkin/reporter2:zipkin-sender-urlconnection",
    ],
    visibility = ["//visibility:public"]
)


java_library(
    name = "client-instrumentation",
    srcs = glob(["instrumentation/ClientInterceptor.java", "util/*.java"]),
    deps = [
        "@graknlabs_grakn_core//protocol:protocol-java",
        "//dependencies/maven/artifacts/io/grpc:grpc-core",
        "//dependencies/maven/artifacts/com/google/protobuf:protobuf-java",
        "//dependencies/maven/artifacts/io/zipkin/brave:brave",
        "//dependencies/maven/artifacts/io/zipkin/zipkin2:zipkin",
        "//dependencies/maven/artifacts/io/zipkin/reporter2:zipkin-reporter",
        "//dependencies/maven/artifacts/io/zipkin/reporter2:zipkin-sender-urlconnection",
        "//dependencies/maven/artifacts/org/slf4j:slf4j-api",
    ],
    visibility = ["//visibility:public"]
)
