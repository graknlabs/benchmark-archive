java_test(
    name = "benchmark-bootup-test-integration",
    test_class = "grakn.benchmark.profiler.GraknBenchmarkTest",
    srcs = ["BenchmarkBootupTestIntegration.java"],
    deps = [
        "//profiler:benchmark-profiler",
        "//dependencies/maven/artifacts/commons-cli",
    ],
    classpath_resources = ["//profiler/test/resources:logback-test"],
    data = [
        "//profiler/test/resources:web-content-config"
    ]
)

java_test(
    name = "benchmark-test-integration",
    test_class = "grakn.benchmark.profiler.BenchmarkTestIntegration",
    srcs = ["BenchmarkTestIntegration.java"],
    deps = [
        "//profiler:benchmark-profiler",
        "//dependencies/maven/artifacts/grakn/core:client",
        "//dependencies/maven/artifacts/grakn/core:grakn-graql",
        "//dependencies/maven/artifacts/grakn/core:util",
        "//dependencies/maven/artifacts/commons-cli"
    ],
    classpath_resources = ["//profiler/test/resources:logback-test"],
    data = [
        "//profiler/test/resources:web-content-config"
    ]
)