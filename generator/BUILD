java_library(
    name = "data-generator",
    srcs = glob(["**/*.java", "*.java"]),
    deps = [
        "//common/timer:timer",

        "@graknlabs_client_java//:client-java",
        "@graknlabs_grakn_core//api:api",
        "@graknlabs_grakn_core//common:common",
        "@graknlabs_grakn_core//concept:concept",
        "@graknlabs_graql//java:graql",

        "//common/analysis:insert-query-analyser",

        "//dependencies/maven/artifacts/org/slf4j:slf4j-api",
        "//dependencies/maven/artifacts/org/apache/commons:commons-math3",
        "//dependencies/maven/artifacts/org/apache/ignite:ignite-core",
        "//dependencies/maven/artifacts/org/apache/ignite:ignite-indexing",
        "//dependencies/maven/artifacts/org/apache/ignite:ignite-slf4j",
    ],
    runtime_deps = [
        "//dependencies/maven/artifacts/io/grpc:grpc-netty-shaded"
    ],
    visibility = ["//visibility:public"]
)
