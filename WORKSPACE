#
#  GRAKN.AI - THE KNOWLEDGE GRAPH
#  Copyright (C) 2019 Grakn Labs Ltd
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

workspace(name = "graknlabs_benchmark")


###########################
# Grakn Labs dependencies #
###########################

load(
    "//dependencies/graknlabs:dependencies.bzl",
    "graknlabs_build_tools",
    "graknlabs_grakn_core",
    "graknlabs_client_java",
    "graknlabs_protocol",
    "graknlabs_common"
)
graknlabs_build_tools()
graknlabs_grakn_core()
graknlabs_client_java()
graknlabs_protocol()
graknlabs_common()

load("@graknlabs_build_tools//distribution:dependencies.bzl", "graknlabs_bazel_distribution")
graknlabs_bazel_distribution()


##################################
# Load Distribution dependencies #
##################################

# needed by deploy_github rule transitively
load("@graknlabs_bazel_distribution//github:dependencies.bzl", "tcnksm_ghr")
tcnksm_ghr()

load("@graknlabs_bazel_distribution//common:dependencies.bzl", "bazelbuild_rules_pkg")
bazelbuild_rules_pkg()

###########################
# Load Bazel dependencies #
###########################

load("@graknlabs_build_tools//bazel:dependencies.bzl", "bazel_common", "bazel_deps", "bazel_toolchain")
bazel_common()
bazel_deps()
bazel_toolchain()

#################################
# Load Build Tools dependencies #
#################################

load("@graknlabs_build_tools//checkstyle:dependencies.bzl", "checkstyle_dependencies")
checkstyle_dependencies()

load("@graknlabs_build_tools//bazel:dependencies.bzl", "bazel_rules_python")
bazel_rules_python()

load("@io_bazel_rules_python//python:pip.bzl", "pip_repositories", "pip_import")
pip_repositories()

pip_import(
    name = "graknlabs_build_tools_ci_pip",
    requirements = "@graknlabs_build_tools//ci:requirements.txt",
)
load("@graknlabs_build_tools_ci_pip//:requirements.bzl",
graknlabs_build_tools_ci_pip_install = "pip_install")
graknlabs_build_tools_ci_pip_install()

##########################
# Load GRPC dependencies #
##########################

load("@graknlabs_build_tools//grpc:dependencies.bzl", "grpc_dependencies")
grpc_dependencies()

load("@com_github_grpc_grpc//bazel:grpc_deps.bzl",
com_github_grpc_grpc_deps = "grpc_deps")
com_github_grpc_grpc_deps()

load("@stackb_rules_proto//java:deps.bzl", "java_grpc_compile")
java_grpc_compile()

#####################################
# Load Java dependencies from Maven #
#####################################
# Load local dependencies before any other external repo deps
# so that versions of local deps have higher priority over external versions
load("//dependencies/maven:dependencies.bzl", "maven_dependencies")
maven_dependencies()

################################
# Load Grakn Core dependencies #
################################

load("@graknlabs_grakn_core//dependencies/maven:dependencies.bzl",
graknlabs_grakn_core_maven_dependencies = "maven_dependencies")
graknlabs_grakn_core_maven_dependencies()

load("@graknlabs_build_tools//bazel:dependencies.bzl", "bazel_rules_docker")
bazel_rules_docker()

load("@graknlabs_grakn_core//dependencies/graknlabs:dependencies.bzl",
"graknlabs_graql", "graknlabs_console")
graknlabs_graql()
graknlabs_console()


###########################
# Load Graql dependencies #
###########################

load("@graknlabs_graql//dependencies/compilers:dependencies.bzl", "antlr_dependencies")
antlr_dependencies()

load("@rules_antlr//antlr:deps.bzl", "antlr_dependencies")
antlr_dependencies()

load("@graknlabs_graql//dependencies/maven:dependencies.bzl",
graknlabs_graql_maven_dependencies = "maven_dependencies")
graknlabs_graql_maven_dependencies()


#####################################
# Load Bazel common workspace rules #
#####################################

# TODO: Figure out why this cannot be loaded at earlier at the top of the file
load("@com_github_google_bazel_common//:workspace_defs.bzl", "google_common_workspace_rules")
google_common_workspace_rules()





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

