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

java_test(
    name = "profiler-bootup-test-integration",
    test_class = "grakn.benchmark.profiler.ProfilerBootupIT",
    srcs = ["ProfilerBootupIT.java"],
    deps = [
        "//profiler/src:benchmark-profiler",
        "//common/configuration:benchmark-configuration",
        "//common/exception:benchmark-exception",
        "@graknlabs_client_java//:client-java",
        "@graknlabs_graql//java:graql",
        "//dependencies/maven/artifacts/commons-cli",
    ],
    classpath_resources = ["//profiler/test-integration/resources:logback-test"],
    data = [
        "//profiler/test-integration/resources:web-content-config"
    ]
)

java_test(
    name = "profiler-static-data-import-it",
    test_class = "grakn.benchmark.profiler.DataImportIT",
    srcs = ["DataImportIT.java"],
    deps = [
        "@graknlabs_client_java//:client-java",
        "//profiler/src:benchmark-profiler",
        "//common/configuration:benchmark-configuration",
        "@graknlabs_graql//java:graql",
        "//dependencies/maven/artifacts/commons-cli",
    ],
    classpath_resources = ["//profiler/test-integration/resources:logback-test"],
    data = [
        "//profiler/test-integration/resources:web-content-config"
    ]
)
