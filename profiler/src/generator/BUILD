java_library(
    name = "data-generator",
    srcs = glob(["**/*.java", "*.java"]),
    deps = [
        "//dependencies/maven/artifacts/grakn/core:client",
        "//dependencies/maven/artifacts/grakn/core:grakn-graql",
        "//dependencies/maven/artifacts/grakn/core:util",

        "//dependencies/maven/artifacts/org/slf4j:slf4j-api",
        "//dependencies/maven/artifacts/org/apache/commons:commons-math3",
        "//dependencies/maven/artifacts/org/apache/ignite:ignite-core",
        "//dependencies/maven/artifacts/org/apache/ignite:ignite-indexing",
        "//dependencies/maven/artifacts/org/apache/ignite:ignite-slf4j"
    ],
    runtime_deps = [
        "//dependencies/maven/artifacts/io/grpc:grpc-netty-shaded"
    ],
    visibility = ["//visibility:public"]
)
