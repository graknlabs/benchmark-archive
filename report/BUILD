java_library(
    name = "report-generator",
    srcs = glob(["*.java"]),
    deps = [
        "//generator:data-generator",
        "//common/configuration:benchmark-configuration",
        "@graknlabs_client_java//:client-java",
        "@graknlabs_grakn_core//api:api",

        "//dependencies/maven/artifacts/commons-cli",
        "//dependencies/maven/artifacts/org/slf4j:slf4j-api",
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
