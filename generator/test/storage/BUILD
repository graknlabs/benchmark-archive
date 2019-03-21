
java_test(
    name = "ignite-concept-store-test",
    test_class = "grakn.benchmark.generator.storage.IgniteConceptStorageTest",
    srcs = ["IgniteConceptStorageTest.java"],

    deps = [
        "@graknlabs_grakn_core//concept:concept",
        "//generator:data-generator",
        "//dependencies/maven/artifacts/org/mockito:mockito-core",
        "//dependencies/maven/artifacts/org/apache/ignite:ignite-core",
        "//dependencies/maven/artifacts/org/apache/ignite:ignite-indexing",
        "//dependencies/maven/artifacts/org/hamcrest:hamcrest-all"
    ]
)
