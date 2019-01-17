
java_test(
    name = "ignite-conceptid-store-test",
    test_class = "grakn.benchmark.runner.storage.IgniteConceptIdStoreTest",
    srcs = ["IgniteConceptIdStoreTest.java"],

    deps = [
        "//runner:benchmark-runner",
        "//dependencies/maven/artifacts/org/mockito:mockito-core",
        "//dependencies/maven/artifacts/grakn/core:grakn-graql",
        "//dependencies/maven/artifacts/org/apache/ignite:ignite-core",
        "//dependencies/maven/artifacts/org/apache/ignite:ignite-indexing",
        "//dependencies/maven/artifacts/org/hamcrest:hamcrest-all"
    ]
)

java_test(
    name = "insertion-analysis-test",
    test_class = "grakn.benchmark.runner.storage.InsertionAnalysisTest",
    srcs = ["InsertionAnalysisTest.java"],
    deps = [
        "//runner:benchmark-runner",
        "//dependencies/maven/artifacts/org/mockito:mockito-core",
        "//dependencies/maven/artifacts/grakn/core:grakn-graql",
        "//dependencies/maven/artifacts/org/hamcrest:hamcrest-all"
    ]
)