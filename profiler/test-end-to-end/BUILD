java_test(
    name = "test-end-to-end",
    test_class = "grakn.benchmark.profiler.GraknBenchmarkE2E",
    srcs = ["GraknBenchmarkE2E.java"],
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