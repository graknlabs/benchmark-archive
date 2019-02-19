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

############################
# Load Common Dependencies #
############################
load("@bazel_tools//tools/build_defs/repo:git.bzl", "git_repository")


####################
# Load Build Tools #
####################
# Load additional build tools, such bazel-deps and unused-deps
load("//dependencies/tools:dependencies.bzl", "tools_dependencies")
tools_dependencies()



#####################################
# Load Java dependencies from Maven #
#####################################
load("//dependencies/maven:dependencies.bzl", "maven_dependencies")
maven_dependencies()

# TODO remove this when graknlabs/benchmark issue #58 is resolved
git_repository(
    name = "bazel_skylib",
    remote = "https://github.com/bazelbuild/bazel-skylib.git",
    tag = "0.1.0",  # change this to use a different release
)

##############################################
# Load External Dependencies - ES and Zipkin #
##############################################
load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_file")
http_file(
  name = "external-dependencies-zipkin",
  sha256 = "0c7374621e751b6f5b70b3bc6cf43f2d105f3e2337c78a0fa571b2263704c9e5",
  urls = [
    "https://dl.bintray.com/openzipkin/maven/io/zipkin/java/zipkin-server/2.11.12/zipkin-server-2.11.12-exec.jar"
  ],
)
http_file(
  name = "external-dependencies-elasticsearch",
  sha256 = "126b83ec1163dba1551491828b9b5f5c456d62f6e731159da045b46570295e38",
  urls = [
    "https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-6.3.2.zip"
  ],
)


##################################
# Load Distribution Dependencies #
##################################

load("//dependencies/distribution:dependencies.bzl", "distribution_dependencies")
distribution_dependencies()

load("@graknlabs_bazel_distribution//github:dependencies.bzl", "github_dependencies_for_deployment")
github_dependencies_for_deployment()

# Why does it break when we move thie declaration after loading tools_dependencies?
load("@com_github_google_bazel_common//:workspace_defs.bzl", "google_common_workspace_rules")
google_common_workspace_rules()


