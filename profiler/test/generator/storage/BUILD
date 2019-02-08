
java_test(
    name = "ignite-conceptid-store-test",
    test_class = "grakn.benchmark.runner.storage.IgniteConceptIdStoreTest",
    srcs = ["IgniteConceptStorageTest.java"],

    deps = [
        "//profiler:benchmark-profiler",
        "//dependencies/maven/artifacts/org/mockito:mockito-core",
        "//dependencies/maven/artifacts/grakn/core:grakn-graql",
        "//dependencies/maven/artifacts/org/apache/ignite:ignite-core",
        "//dependencies/maven/artifacts/org/apache/ignite:ignite-indexing",
        "//dependencies/maven/artifacts/org/hamcrest:hamcrest-all"
    ]
)

java_test(
    name = "insertion-analysis-test",
    test_class = "grakn.benchmark.runner.storage.InsertQueryAnalyserTest",
    srcs = ["InsertQueryAnalyserTest.java"],
    deps = [
        "//profiler:benchmark-profiler",
        "//dependencies/maven/artifacts/org/mockito:mockito-core",
        "//dependencies/maven/artifacts/grakn/core:grakn-graql",
        "//dependencies/maven/artifacts/org/hamcrest:hamcrest-all"
    ]
)