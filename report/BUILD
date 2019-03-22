java_library(
    name = "report-generator",
    srcs = glob(["*.java"]),
    deps = [
        "//generator:data-generator",
        "//common/configuration:benchmark-configuration",
        "//common/exception:benchmark-exception",

        "@graknlabs_client_java//:client-java",
        "@graknlabs_grakn_core//api:api",
        "@graknlabs_graql//java:graql",
        "@graknlabs_grakn_core//concept:concept",

        "//dependencies/maven/artifacts/org/apache/ignite:ignite-core",

        "//dependencies/maven/artifacts/commons-cli",
        "//dependencies/maven/artifacts/org/slf4j:slf4j-api",

        "//dependencies/maven/artifacts/com/fasterxml/jackson/core:jackson-core",
        "//dependencies/maven/artifacts/com/fasterxml/jackson/core:jackson-databind",
    ],
    visibility = ["//visibility:public"]
)

java_binary(
    name = "report-generator-binary",
    main_class = "grakn.benchmark.report.ReportGenerator",
    runtime_deps = [
        "//report:report-generator",
    ],
    classpath_resources = ["//profiler/test/resources:logback-test"],
    data = [
        "//common/conf:biochemical-network",
        "//common/conf:financial-transactions",
        "//common/conf:road-network",
        "//common/conf:social-network",
        "//common/conf:complex-network",
    ],
    visibility = ["//visibility:public"]
)
