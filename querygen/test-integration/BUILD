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

java_test(
     name = "query-generator-it",
     test_class = "grakn.benchmark.querygen.QueryGeneratorIT",
     srcs = ["QueryGeneratorIT.java"],
     deps = [
         "//querygen/src:query-sampler-lib",
         "@graknlabs_client_java//:client-java",
         "@graknlabs_graql//java:graql",
         "@graknlabs_grakn_core//test-integration/rule:grakn-test-server"
     ],
     classpath_resources = [
         "//querygen/test-integration/resources:logback-test"
     ],
     data = [
         "//querygen/test-integration/conf:cassandra-embedded.yaml",
         "//querygen/test-integration/conf:grakn.properties",
         "//querygen/test-integration/resources:schema.gql"
     ],
     size = "large"
 )


java_test(
    name = "schema-walker-it",
    test_class = "grakn.benchmark.querygen.SchemaWalkerIT",
    srcs = ["SchemaWalkerIT.java"],
    deps = [
        "//querygen/src:query-sampler-lib",
        "@graknlabs_client_java//:client-java",
        "@graknlabs_graql//java:graql",
        "@graknlabs_grakn_core//test-integration/rule:grakn-test-server"
    ],
    classpath_resources = [
        "//querygen/test-integration/resources:logback-test"
    ],
    data = [
        "//querygen/test-integration/conf:cassandra-embedded.yaml",
        "//querygen/test-integration/conf:grakn.properties",
        "//querygen/test-integration/resources:schema.gql"
    ]
)

java_test(
    name = "query-sampler-it",
    test_class = "grakn.benchmark.querygen.QuerySamplerIT",
    srcs = ["QuerySamplerIT.java"],
    deps = [
        "//querygen/src:query-sampler-lib",
        "@graknlabs_client_java//:client-java",
        "@graknlabs_graql//java:graql",
        "@graknlabs_grakn_core//test-integration/rule:grakn-test-server"
    ],
    classpath_resources = [
        "//querygen/test-integration/resources:logback-test"
    ],
    data = [
        "//querygen/test-integration/conf:cassandra-embedded.yaml",
        "//querygen/test-integration/conf:grakn.properties",
        "//querygen/test-integration/resources:schema.gql"
    ],
    size = "large"
)

checkstyle_test(
    name = "checkstyle",
    targets = [
        ":query-generator-it",
        ":schema-walker-it",
        ":query-sampler-it",
    ],
    license_type = "agpl"
)