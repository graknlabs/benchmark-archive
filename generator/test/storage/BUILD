load("@graknlabs_build_tools//checkstyle:rules.bzl", "checkstyle_test")

java_test(
    name = "ignite-concept-store-test",
    test_class = "grakn.benchmark.generator.storage.IgniteConceptStorageTest",
    srcs = ["IgniteConceptStorageTest.java"],

    deps = [
        "@graknlabs_client_java//:client-java",
        "//generator:data-generator",
        "//dependencies/maven/artifacts/org/mockito:mockito-core",
        "//dependencies/maven/artifacts/org/apache/ignite:ignite-core",
        "//dependencies/maven/artifacts/org/apache/ignite:ignite-indexing",
        "//dependencies/maven/artifacts/org/hamcrest:hamcrest-all"
    ]
)

checkstyle_test(
    name = "checkstyle",
    targets = [
        ":ignite-concept-store-test"
    ],
    license_type = "agpl"
)
