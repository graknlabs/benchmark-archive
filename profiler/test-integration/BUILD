java_test(
    name = "profiler-bootup-test-integration",
    test_class = "grakn.benchmark.profiler.ProfilerBootupIT",
    srcs = ["ProfilerBootupIT.java"],
    deps = [
        "//profiler/src:benchmark-profiler",
        "//common/configuration:benchmark-configuration",
        "//common/exception:benchmark-exception",
        "@graknlabs_client_java//:client-java",
        "@graknlabs_grakn_core//concept:concept",
        "@graknlabs_graql//java:graql",
        "//dependencies/maven/artifacts/commons-cli",
    ],
    classpath_resources = ["//profiler/test-integration/resources:logback-test"],
    data = [
        "//profiler/test-integration/resources:web-content-config"
    ]
)

java_test(
    name = "profiler-static-data-import-it",
    test_class = "grakn.benchmark.profiler.DataImportIT",
    srcs = ["DataImportIT.java"],
    deps = [
        "//profiler/src:benchmark-profiler",
        "//common/configuration:benchmark-configuration",
        "@graknlabs_client_java//:client-java",
        "@graknlabs_grakn_core//concept:concept",
        "@graknlabs_graql//java:graql",
        "//dependencies/maven/artifacts/commons-cli",
    ],
    classpath_resources = ["//profiler/test-integration/resources:logback-test"],
    data = [
        "//profiler/test-integration/resources:web-content-config"
    ]
)
