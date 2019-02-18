#
# GRAKN.AI - THE KNOWLEDGE GRAPH
# Copyright (C) 2018 Grakn Labs Ltd
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
        "//profiler:conf/road_network/queries.yml": "conf/road_network/queries.yml",
        "//profiler:conf/road_network/road_config.yml": "conf/road_network/road_config.yml",
        "//profiler:conf/road_network/road_network.gql": "conf/road_network/road_network.gql",

        "//profiler:conf/social_network/queries.yml": "conf/social_network/queries.yml",
        "//profiler:conf/social_network/social_network_config.yml": "conf/social_network/social_network_config.yml",
        "//profiler:conf/social_network/social_network.gql": "conf/social_network/social_network.gql",

        "//profiler:conf/financial_transactions/queries.yml": "conf/financial_transactions/queries.yml",
        "//profiler:conf/financial_transactions/financial_config.yml": "conf/financial_transactions/financial_config.yml",
        "//profiler:conf/financial_transactions/financial.gql": "conf/financial_transactions/financial.gql",

        "//profiler:conf/biochemical_network/queries.yml": "conf/biochemical_network/queries.yml",
        "//profiler:conf/biochemical_network/biochemical_config.yml": "conf/biochemical_network/biochemical_config.yml",
        "//profiler:conf/biochemical_network/biochemical_network.gql": "conf/biochemical_network/biochemical_network.gql",

        "//profiler:logback": "conf/logback.xml",

        # External dependencies: Elasticsearch and Zipkin
        "//profiler:setup.sh": "external-dependencies/setup.sh",
        "@external-dependencies-zipkin//file": "external-dependencies/zipkin.jar",
        "@external-dependencies-elasticsearch//file": "external-dependencies/elasticsearch.zip"
    },
    output_filename = "benchmark",
)