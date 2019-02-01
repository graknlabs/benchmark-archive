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

# TODO: the distribution only includes 'benchmark-runner'. Need to add 'benchmark-dashboard'.
load("@graknlabs_rules_deployment//distribution:rules.bzl", distribution = "distribution")
distribution(
    name = "distribution",
    targets = {
        "//runner:benchmark-runner": "lib/",
    },
    additional_files = {
        "//runner:benchmark": "benchmark",
        "//runner:conf/road_network/queries.yml": "conf/road_network/queries.yml",
        "//runner:conf/road_network/road_config.yml": "conf/road_network/road_config.yml",
        "//runner:conf/road_network/road_network.gql": "conf/road_network/road_network.gql",

        "//runner:conf/social_network/queries.yml": "conf/social_network/queries.yml",
        "//runner:conf/social_network/social_network_config.yml": "conf/social_network/social_network_config.yml",
        "//runner:conf/social_network/social_network.gql": "conf/social_network/social_network.gql",

        "//runner:conf/financial_transactions/queries.yml": "conf/financial_transactions/queries.yml",
        "//runner:conf/financial_transactions/financial_config.yml": "conf/financial_transactions/financial_config.yml",
        "//runner:conf/financial_transactions/financial.gql": "conf/financial_transactions/financial.gql",

        "//runner:conf/biochem_network/queries.yml": "conf/biochem_network/queries.yml",
        "//runner:conf/biochem_network/biochem_config.yml": "conf/biochem_network/biochem_config.yml",
        "//runner:conf/biochem_network/biochem_network.gql": "conf/biochem_network/biochem_network.gql",

        "//runner:logback": "conf/logback.xml",

        # External dependencies: Elasticsearch and Zipkin
        "//runner:setup.sh": "external-dependencies/setup.sh",
        "@external-dependencies-zipkin//file": "external-dependencies/zipkin.jar",
        "@external-dependencies-elasticsearch//file": "external-dependencies/elasticsearch.zip"
    },
    output_filename = "benchmark",
)