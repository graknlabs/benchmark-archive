load("@graknlabs_build_tools//checkstyle:rules.bzl", "checkstyle_test")

java_test(
    name = "central-stream-provider-test",
    test_class = "grakn.benchmark.generator.provider.key.CentralConceptProviderTest",
    srcs = ["CentralConceptProviderTest.java"],
    deps = [
        "//generator:data-generator",
        "//dependencies/maven/artifacts/org/mockito:mockito-core",
    ]
)

java_test(
    name = "conceptid-storage-provider-test",
    test_class = "grakn.benchmark.generator.provider.key.ConceptKeyStorageProviderTest",
    srcs = ["ConceptKeyStorageProviderTest.java"],
    deps = [
        "//generator:data-generator",
        "//dependencies/maven/artifacts/org/mockito:mockito-core",
    ]
)

java_test(
     name = "not-in-relationship-conceptid-storage-provider-test",
     test_class = "grakn.benchmark.generator.provider.key.NotInRelationshipConceptKeyProviderTest",
     srcs = ["NotInRelationshipConceptKeyProviderTest.java"],
     deps = [
        "//generator:data-generator",
        "//dependencies/maven/artifacts/org/mockito:mockito-core",
     ]
 )

checkstyle_test(
    name = "checkstyle",
    targets = [
        ":central-stream-provider-test",
        ":conceptid-storage-provider-test",
        ":not-in-relationship-conceptid-storage-provider-test",
    ],
    license_type = "agpl"
)
