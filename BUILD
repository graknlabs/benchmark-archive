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

exports_files(
    [
        "VERSION",
        "deployment.properties",
    ],
    visibility = ["//visibility:public"],
)

# TODO: Need to add 'benchmark-dashboard' as a distribution.
load("@graknlabs_bazel_distribution//common:rules.bzl", "assemble_zip")

assemble_zip(
    name = "profiler-distribution",
    additional_files = {
        "//profiler:benchmark": "benchmark",
        "//common/configuration/scenario:road_network/queries_read.yml": "scenario/road_network/queries_read.yml",
        "//common/configuration/scenario:road_network/queries_write.yml": "scenario/road_network/queries_write.yml",
        "//common/configuration/scenario:road_network/road_config_read.yml": "scenario/road_network/road_config_read.yml",
        "//common/configuration/scenario:road_network/road_config_write.yml": "scenario/road_network/road_config_write.yml",
        "//common/configuration/scenario:road_network/road_network.gql": "scenario/road_network/road_network.gql",
        "//common/configuration/scenario:biochemical_network/queries_read.yml": "scenario/biochemical_network/queries_read.yml",
        "//common/configuration/scenario:biochemical_network/biochemical_config_read.yml": "scenario/biochemical_network/biochemical_config_read.yml",
        "//common/configuration/scenario:biochemical_network/biochemical_network.gql": "scenario/biochemical_network/biochemical_network.gql",
        "//common/configuration/scenario:complex/queries_complex_read.yml": "scenario/complex/queries_complex_read.yml",
        "//common/configuration/scenario:complex/queries_complex_write.yml": "scenario/complex/queries_complex_write.yml",
        "//common/configuration/scenario:complex/config_read.yml": "scenario/complex/config_read.yml",
        "//common/configuration/scenario:complex/config_write.yml": "scenario/complex/config_write.yml",
        "//common/configuration/scenario:complex/schema.gql": "scenario/complex/schema.gql",
        "//common/configuration/scenario:reasoning/queries_read.yml": "scenario/reasoning/queries_read.yml",
        "//common/configuration/scenario:reasoning/transitivity_data.gql": "scenario/reasoning/transitivity_data.gql",
        "//common/configuration/scenario:reasoning/config_read.yml": "scenario/reasoning/config_read.yml",
        "//common/configuration/scenario:reasoning/transitivity_schema.gql": "scenario/reasoning/transitivity_schema.gql",
        "//common/configuration/scenario:rule_scaling/queries_read.yml": "scenario/rule_scaling/queries_read.yml",
        "//common/configuration/scenario:rule_scaling/rule_scaling_data.gql": "scenario/rule_scaling/rule_scaling_data.gql",
        "//common/configuration/scenario:rule_scaling/config_read.yml": "scenario/rule_scaling/config_read.yml",
        "//common/configuration/scenario:rule_scaling/rule_scaling_schema.gql": "scenario/rule_scaling/rule_scaling_schema.gql",

        "//common/configuration/scenario:schema/data_definition_config.yml": "scenario/schema/data_definition_config.yml",
        "//common/configuration/scenario:schema/data_definition_schema.gql": "scenario/schema/data_definition_schema.gql",
        "//common/configuration/scenario:schema/queries.yml": "scenario/schema/queries.yml",

        "//common/configuration/scenario:attribute/attribute_read_config.yml": "scenario/attribute/attribute_read_config.yml",
        "//common/configuration/scenario:attribute/attribute_schema.gql": "scenario/attribute/attribute_schema.gql",
        "//common/configuration/scenario:attribute/queries_read.yml": "scenario/attribute/queries_read.yml",


        # External dependencies: Elasticsearch and Zipkin
        "//profiler:setup.sh": "external-dependencies/setup.sh",
        "@external-dependencies-zipkin//file": "external-dependencies/zipkin.jar",
        "@external-dependencies-elasticsearch//file": "external-dependencies/elasticsearch.zip",
    },
    output_filename = "profiler",
    targets = ["//profiler:profiler-deps"],
    visibility = ["//visibility:public"],
)

assemble_zip(
    name = "report-producer-distribution",
    additional_files = {
        "//report/producer:report_producer": "report_producer",
        "//common/configuration/scenario:road_network/queries_read.yml": "scenario/road_network/queries_read.yml",
        "//common/configuration/scenario:road_network/queries_write.yml": "scenario/road_network/queries_write.yml",
        "//common/configuration/scenario:road_network/road_network.gql": "scenario/road_network/road_network.gql",
        "//common/configuration/scenario:road_network/road_config_read.yml": "scenario/road_network/road_config_read.yml",
        "//common/configuration/scenario:road_network/road_config_read_c4.yml": "scenario/road_network/road_config_read_c4.yml",
        "//common/configuration/scenario:road_network/road_config_read_c8.yml": "scenario/road_network/road_config_read_c8.yml",
        "//common/configuration/scenario:road_network/road_config_write.yml": "scenario/road_network/road_config_write.yml",
        "//common/configuration/scenario:road_network/road_config_write_c4.yml": "scenario/road_network/road_config_write_c4.yml",
        "//common/configuration/scenario:road_network/road_config_write_c8.yml": "scenario/road_network/road_config_write_c8.yml",
        "//common/configuration/scenario:complex/queries_complex_read.yml": "scenario/complex/queries_complex_read.yml",
        "//common/configuration/scenario:complex/queries_complex_write.yml": "scenario/complex/queries_complex_write.yml",
        "//common/configuration/scenario:complex/schema.gql": "scenario/complex/schema.gql",
        "//common/configuration/scenario:complex/config_read.yml": "scenario/complex/config_read.yml",
        "//common/configuration/scenario:complex/config_read_c4.yml": "scenario/complex/config_read_c4.yml",
        "//common/configuration/scenario:complex/config_read_c8.yml": "scenario/complex/config_read_c8.yml",
        "//common/configuration/scenario:complex/config_write.yml": "scenario/complex/config_write.yml",
        "//common/configuration/scenario:complex/config_write_c4.yml": "scenario/complex/config_write_c4.yml",
        "//common/configuration/scenario:complex/config_write_c8.yml": "scenario/complex/config_write_c8.yml",

        # include these in case the scripts want to add these to the report
        "//common/configuration/scenario:biochemical_network/queries_read.yml": "scenario/biochemical_network/queries_read.yml",
        "//common/configuration/scenario:biochemical_network/biochemical_config_read.yml": "scenario/biochemical_network/biochemical_config_read.yml",
        "//common/configuration/scenario:biochemical_network/biochemical_network.gql": "scenario/biochemical_network/biochemical_network.gql",
    },
    output_filename = "report-producer",
    targets = ["//report/producer:report-deps"],
    visibility = ["//visibility:public"],
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
