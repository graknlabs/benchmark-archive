java_library(
    name = "benchmark-configuration",
#    srcs = glob(["*.java"]),
    srcs = [
        "BenchmarkConfigurationFile.java",
        "BenchmarkConfiguration.java",
        "BenchmarkArguments.java",
        "ConfigurationException.java",
        "QueriesConfigurationFile.java"
    ],
    deps = [
        "//dependencies/maven/artifacts/com/fasterxml/jackson/core:jackson-core",
        "//dependencies/maven/artifacts/com/fasterxml/jackson/core:jackson-databind",
        "//dependencies/maven/artifacts/com/fasterxml/jackson/dataformat:jackson-dataformat-yaml",
        "//dependencies/maven/artifacts/commons-cli",
    ],
    visibility = ["//visibility:public"]
)