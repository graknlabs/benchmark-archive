#
# GRAKN.AI - THE KNOWLEDGE GRAPH
# Copyright (C) 2019 Grakn Labs Ltd
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

java_library(
    name = "query-generator",
    srcs = glob(["*.java"]),
    deps = [
        "@graknlabs_client_java//:client-java",
        "@graknlabs_graql//java:graql",
        "@graknlabs_grakn_core//api:api",
        "@graknlabs_grakn_core//concept:concept",

        # together these can be used to create clients with tracing enabled
        "//dependencies/maven/artifacts/org/slf4j:slf4j-api",
#        "//dependencies/maven/artifacts/commons-cli",
    ],
    runtime_deps = [
        "//dependencies/maven/artifacts/io/grpc:grpc-netty-shaded",
        "//dependencies/maven/artifacts/ch/qos/logback:logback-classic",
    ],
    visibility = ["//visibility:public"]
)