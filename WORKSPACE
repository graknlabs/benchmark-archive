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

workspace(name = "benchmark")

# Load additional build tools, such bazel-deps and unused-deps
load("//dependencies/tools:dependencies.bzl", "tools_dependencies")
tools_dependencies()
#
#
load("//dependencies/maven:dependencies.bzl", "maven_dependencies")
maven_dependencies()


# --- Grakn client-java ---
#maven_jar(
#    name = "grakn_client_java",
#    artifact = "ai.grakn:client-java:1.5.0-SNAPSHOT",
#    repository = "http://maven.grakn.ai/nexus/content/repositories/bazel-test-snapshot/"
#)

# --- Logging ---
maven_jar(
    name = "slf4j",
    artifact = "org.slf4j:slf4j-api:1.7.25"
)

# --- Ignite ---
maven_jar(
    name = "ignite_core",
    artifact = "org.apache.ignite:ignite-core:2.6.0"
)
maven_jar(
    name = "ignite_indexing",
    artifact = "org.apache.ignite:ignite-indexing:2.6.0"
)

# --- Testing ---
maven_jar(
    name = "mockito_core",
    artifact = "org.mockito:mockito-core:2.6.4"
)

# --- Elasticsearch client ---
maven_jar(
    name = "elasticsearch_rest_client",
    artifact = "org.elasticsearch.client:elasticsearch-rest-client:6.4.0"
)
# required HTTP libraries
maven_jar(
    name = "apache_httpcore",
    artifact = "org.apache.httpcomponents:httpcore:4.4.10"
)
maven_jar(
    name = "apache_httpclient",
    artifact = "org.apache.httpcomponents:httpclient:4.5.6"
)

# --- Apache CLI commons for cmd line options parsing ---
maven_jar(
    name = "apache_commons_cli",
    artifact = "commons-cli:commons-cli:1.3.1"
)

# --- Apache commons math 3 for Zipf distribution ---
maven_jar(
    name = "apache_commons_math3",
    artifact = "org.apache.commons:commons-math3:3.6.1"
)

# --- Brave/Zipkin tracing ---
maven_jar(
    name = "brave",
    artifact = "io.zipkin.brave:brave:5.1.2"
)



