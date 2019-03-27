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

exports_files(["benchmark", "setup.sh"], visibility = ["//visibility:public"])

java_binary(
    name = "benchmark-profiler-binary",
    runtime_deps = [
        "//profiler/src:benchmark-profiler"
    ],
    main_class = "grakn.benchmark.profiler.GraknBenchmark",
    classpath_resources = ["//profiler/src:logback"],
    visibility = ["//visibility:public"]
)
