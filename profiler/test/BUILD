java_test(
    name = "profiler-bootup-test-integration",
    test_class = "grakn.benchmark.profiler.ProfilerBootupTestIntegration",
    srcs = ["ProfilerBootupTestIntegration.java"],
    deps = [
        "//profiler/src:benchmark-profiler",
        "@graknlabs_client_java//:client-java",
        "@graknlabs_grakn_core//concept:concept",
        "@graknlabs_graql//java:graql",
        "//dependencies/maven/artifacts/commons-cli",
    ],
    classpath_resources = ["//profiler/test/resources:logback-test"],
    data = [
        "//profiler/test/resources:web-content-config"
    ]
)

java_test(
    name = "profiler-bootup-test",
    test_class = "grakn.benchmark.profiler.ProfilerBootupTest",
    srcs = ["ProfilerBootupTest.java"],
    deps = [
        "//profiler/src:benchmark-profiler",
        "//dependencies/maven/artifacts/commons-cli",
    ],
    classpath_resources = ["//profiler/test/resources:logback-test"],
    data = [
        "//profiler/test/resources:web-content-config"
    ]
)

