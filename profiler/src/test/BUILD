java_test(
    name = "benchmark-bootup-test-integration",
    test_class = "grakn.benchmark.profiler.test.BenchmarkBootupTestIntegration",
    srcs = ["BenchmarkBootupTestIntegration.java"],
    deps = [
        "//profiler:benchmark-profiler",
        "//dependencies/maven/artifacts/commons-cli",
    ],
    classpath_resources = ["//profiler/src/test/resources:logback-test"],
    data = [
        "//profiler/src/test/resources:web-content-config"
    ]
)

java_test(
    name = "benchmark-test-integration",
    test_class = "grakn.benchmark.profiler.test.BenchmarkTestIntegration",
    srcs = ["BenchmarkTestIntegration.java"],
    deps = [
        "//profiler:benchmark-profiler",
        "@graknlabs_client_java//:client-java",
        "@graknlabs_grakn_core//concept:concept",
        "@graknlabs_graql//java:graql",
        "//dependencies/maven/artifacts/commons-cli",

        "//dependencies/maven/artifacts/io/grpc:grpc-core",
    ],
    classpath_resources = ["//profiler/src/test/resources:logback-test"],
    data = [
        "//profiler/src/test/resources:web-content-config"
    ]
)