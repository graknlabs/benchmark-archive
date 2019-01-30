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
        "//runner:conf/societal_model/queries.yml": "conf/societal_model/queries.yml",
        "//runner:conf/societal_model/societal_config_1.yml": "conf/societal_model/societal_config_1.yml",
        "//runner:conf/societal_model/societal_model.gql": "conf/societal_model/societal_model.gql",
        "//runner:conf/web_content/queries.yml": "conf/web_content/queries.yml",
        "//runner:conf/web_content/web_content_config.yml": "conf/web_content/web_content_config.yml",
        "//runner:conf/web_content/web_content_schema.gql": "conf/web_content/web_content_schema.gql",
        "//runner:logback": "conf/logback.xml",
        
        # External dependencies: Elasticsearch and Zipkin
        "//runner:setup.sh": "external-dependencies/setup.sh",
        "@external-dependencies-zipkin//file": "external-dependencies/zipkin.jar",
        "@external-dependencies-elasticsearch//file": "external-dependencies/elasticsearch.zip"
    },
    output_filename = "benchmark",
)