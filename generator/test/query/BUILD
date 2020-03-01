load("@graknlabs_build_tools//checkstyle:rules.bzl", "checkstyle_test")

java_test(
    name = "attribute-generator-test",
    test_class = "grakn.benchmark.generator.query.AttributeGeneratorTest",
    srcs = ["AttributeGeneratorTest.java"],
    deps = [
        "@graknlabs_graql//java:graql",
        "//generator:data-generator",
        "//dependencies/maven/artifacts/org/mockito:mockito-core",
    ]
)

java_test(
    name = "entity-generator-test",
    test_class = "grakn.benchmark.generator.query.EntityGeneratorTest",
    srcs = ["EntityGeneratorTest.java"],
    deps = [
        "@graknlabs_graql//java:graql",
        "//generator:data-generator",
        "//dependencies/maven/artifacts/org/mockito:mockito-core",
    ]
)

java_test(
    name = "relationship-generator-test",
    test_class = "grakn.benchmark.generator.query.RelationGeneratorTest",
    srcs = ["RelationGeneratorTest.java"],
    deps = [
        "@graknlabs_graql//java:graql",
        "//generator:data-generator",
        "//dependencies/maven/artifacts/org/mockito:mockito-core",
    ]
)

checkstyle_test(
    name = "checkstyle",
    targets = [
        ":attribute-generator-test",
        ":entity-generator-test",
        ":relationship-generator-test",
    ],
    license_type = "agpl"
)
