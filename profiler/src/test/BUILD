java_test(
    name = "benchmark-bootup-test-integration",
    test_class = "grakn.benchmark.profiler.test.GraknBootupTestIntegration",
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
        "//dependencies/maven/artifacts/grakn/core:client",
        "//dependencies/maven/artifacts/grakn/core:server",
        "//dependencies/maven/artifacts/commons-cli",

        "//dependencies/maven/artifacts/io/grpc:grpc-core",
    ],
    runtime_deps = [
#        "//dependencies/maven/artifacts/io/grpc:grpc-netty-shaded",
    ],
    classpath_resources = ["//profiler/src/test/resources:logback-test"],
    data = [
        "//profiler/src/test/resources:web-content-config"
    ]
)