java_test(
    name = "benchmark-test",
    test_class = "grakn.benchmark.runner.GraknBenchmarkTest",
    srcs = ["GraknBenchmarkTest.java"],
    deps = [
        "//runner:benchmark-runner",
        "//dependencies/maven/artifacts/commons-cli",
    ],
    classpath_resources = ["//runner/test/resources:logback-test"],
    data = [
        "//runner/test/resources:web-content-config"
    ]
)