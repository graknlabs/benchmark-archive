java_test(
    name = "test-end-to-end",
    test_class = "grakn.benchmark.runner.SchemaManagerE2E",
    srcs = ["SchemaManagerE2E.java"],
    deps = [
        "//profiler:benchmark-profiler",
        "//dependencies/maven/artifacts/grakn/core:client",
        "//dependencies/maven/artifacts/grakn/core:grakn-graql",
        "//dependencies/maven/artifacts/grakn/core:util",
        "//dependencies/maven/artifacts/commons-cli"
    ],
    classpath_resources = ["//runner/test/resources:logback-test"],
    data = [
        "//runner/test/resources:web-content-config"
    ]
)