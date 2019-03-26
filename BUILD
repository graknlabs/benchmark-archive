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

exports_files(["VERSION", "deployment.properties"], visibility = ["//visibility:public"])

# TODO: the distribution only includes 'benchmark-profiler'. Need to add 'benchmark-dashboard'.
load("@graknlabs_bazel_distribution//distribution:rules.bzl", "distribution_structure", "distribution_zip")


distribution_structure(
    name="benchmark-binary",
    targets = {
        "//profiler:benchmark-profiler-binary": "lib/"
    },
    visibility = ["//:__pkg__"]
)

distribution_zip(
    name = "distribution",
    distribution_structures = [
        "//:benchmark-binary"
    ],
    additional_files = {
        "//profiler:benchmark": "benchmark",

        "//common/configuration/scenario:road-queries-read": "scenario/road_network/queries_read.yml",
        "//common/configuration/scenario:road-queries-write": "scenario/road_network/queries_write.yml",
        "//common/configuration/scenario:road-scenario-read": "scenario/road_network/road_scenario_read.yml",
        "//common/configuration/scenario:road-scenario-write": "scenario/road_network/road_scenario_write.yml",
        "//common/configuration/scenario:road-schema": "scenario/road_network/road_network.gql",

        "//common/configuration/scenario:social-scenario": "scenario/social_network/social_network_scenario_read.yml",
        "//common/configuration/scenario:social-queries": "scenario/social_network/social_network.gql",
        "//common/configuration/scenario:social-schema": "scenario/social_network/social_network.gql",

        "//common/configuration/scenario:financial-queries": "scenario/financial_transactions/queries_read.yml",
        "//common/configuration/scenario:financial-scenario": "scenario/financial_transactions/financial_scenario_read.yml",
        "//common/configuration/scenario:financial-schema": "scenario/financial_transactions/financial.gql",

        "//common/configuration/scenario:biochemical-queries": "scenario/biochemical_network/queries_read.yml",
        "//common/configuration/scenario:biochemical-scenario": "scenario/biochemical_network/biochemical_scenario_read.yml",
        "//common/configuration/scenario:biochemical-schema": "scenario/biochemical_network/biochemical_network.gql",

        "//common/configuration/scenario:complex-queries-read": "scenario/complex/queries_complex_read.yml",
        "//common/configuration/scenario:complex-queries-write": "scenario/complex/queries_complex_write.yml",
        "//common/configuration/scenario:complex-scenario-read": "scenario/complex/scenario_read.yml",
        "//common/configuration/scenario:complex-scenario-write": "scenario/complex/scenario_write.yml",
        "//common/configuration/scenario:complex-schema" : "scenario/complex/schema.gql",

        # External dependencies: Elasticsearch and Zipkin
        "//profiler:setup.sh": "external-dependencies/setup.sh",
        "@external-dependencies-zipkin//file": "external-dependencies/zipkin.jar",
        "@external-dependencies-elasticsearch//file": "external-dependencies/elasticsearch.zip"
    },
    output_filename = "benchmark",
)




# When a Bazel build or test is executed with RBE, it will be executed using the following platform.
# The platform is based on the standard rbe_ubuntu1604 from @bazel_toolchains,
# but with an additional setting dockerNetwork = standard because our tests need network access
platform(
    name = "rbe-platform",
    parents = ["@bazel_toolchains//configs/ubuntu16_04_clang/1.1:rbe_ubuntu1604"],
    remote_execution_properties = """
        {PARENT_REMOTE_EXECUTION_PROPERTIES}
        properties: {
          name: "dockerNetwork"
          value: "standard"
        }
        """,
)
