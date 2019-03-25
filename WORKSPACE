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


##################################
# Load Grakn Dependencies #
##################################


# --- Grakn client-java dep ---
git_repository(
    name = "graknlabs_client_java",
    remote = "https://github.com/graknlabs/client-java.git",
    commit = "bbc8e2eaf99f8e2ecb4fe06813a47dcb36f96071"
)
load("@graknlabs_client_java//dependencies/maven:dependencies.bzl", maven_dependencies_for_build= "maven_dependencies")
maven_dependencies_for_build()

git_repository(
    name = "graknlabs_graql",
    remote = "https://github.com/graknlabs/graql.git",
    commit = "8b21baff544db206443cc953c22767563e42dd99"
)
load("@graknlabs_graql//dependencies/maven:dependencies.bzl", maven_dependencies_for_build= "maven_dependencies")
maven_dependencies_for_build()


# --- Grakn core transitive deps ---
git_repository(
    name = "graknlabs_grakn_core",
    remote = "https://github.com/graknlabs/grakn.git",
    commit = "b9ac28f854b06e8bb67483f9ff1e8b2f744d14b4"
)
load("@graknlabs_grakn_core//dependencies/compilers:dependencies.bzl", "grpc_dependencies")
grpc_dependencies()

load("@com_github_grpc_grpc//bazel:grpc_deps.bzl", com_github_grpc_grpc_bazel_grpc_deps = "grpc_deps")
com_github_grpc_grpc_bazel_grpc_deps()

load("@graknlabs_grakn_core//dependencies/tools:dependencies.bzl", "tools_dependencies")
tools_dependencies()

load("@graknlabs_grakn_core//dependencies/tools/checkstyle:checkstyle.bzl", "checkstyle_dependencies")
checkstyle_dependencies()

load("@graknlabs_grakn_core//dependencies/distribution:dependencies.bzl", "distribution_dependencies")
distribution_dependencies()

load("@graknlabs_bazel_distribution//github:dependencies.bzl", "github_dependencies_for_deployment")
github_dependencies_for_deployment()

load("@com_github_google_bazel_common//:workspace_defs.bzl", "google_common_workspace_rules")
google_common_workspace_rules()

load("@stackb_rules_proto//java:deps.bzl", "java_grpc_compile")
java_grpc_compile()

load("@graknlabs_grakn_core//dependencies/maven:dependencies.bzl", maven_dependencies_for_build = "maven_dependencies")
maven_dependencies_for_build()

load("@graknlabs_grakn_core//dependencies/git:dependencies.bzl", "graknlabs_graql")
graknlabs_graql()

load("@graknlabs_graql//dependencies/compilers:dependencies.bzl", "antlr_dependencies")
antlr_dependencies()

load("@rules_antlr//antlr:deps.bzl", "antlr_dependencies")
antlr_dependencies()

load("@graknlabs_graql//dependencies/maven:dependencies.bzl", graql_dependencies = "maven_dependencies")
graql_dependencies()

load("@stackb_rules_proto//java:deps.bzl", "java_grpc_compile")
java_grpc_compile()

load("@graknlabs_grakn_core//dependencies/docker:dependencies.bzl", "docker_dependencies")
docker_dependencies()
