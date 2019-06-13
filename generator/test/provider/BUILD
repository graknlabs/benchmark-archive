java_test(
    name = "central-stream-provider-test",
    test_class = "grakn.benchmark.generator.provider.key.CentralConceptProviderTest",
    srcs = ["CentralConceptProviderTest.java"],
    deps = [
        "@graknlabs_grakn_core//concept:concept",
        "//generator:data-generator",
        "//dependencies/maven/artifacts/org/mockito:mockito-core",
    ]
)

java_test(
    name = "conceptid-storage-provider-test",
    test_class = "grakn.benchmark.generator.provider.key.ConceptKeyStorageProviderTest",
    srcs = ["ConceptKeyStorageProviderTest.java"],
    deps = [
        "@graknlabs_grakn_core//concept:concept",
        "//generator:data-generator",
        "//dependencies/maven/artifacts/org/mockito:mockito-core",
    ]
)

java_test(
     name = "not-in-relationship-conceptid-storage-provider-test",
     test_class = "grakn.benchmark.generator.provider.key.NotInRelationshipConceptKeyProviderTest",
     srcs = ["NotInRelationshipConceptKeyProviderTest.java"],
     deps = [
        "@graknlabs_grakn_core//concept:concept",
        "//generator:data-generator",
        "//dependencies/maven/artifacts/org/mockito:mockito-core",
     ]
 )
