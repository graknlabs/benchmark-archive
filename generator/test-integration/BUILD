load("@graknlabs_build_tools//checkstyle:rules.bzl", "checkstyle_test")

java_test(
    name = "keyspace-schema-labels-it",
    test_class = "grakn.benchmark.generator.util.KeyspaceSchemaLabelsIT",
    srcs = ["util/KeyspaceSchemaLabelsIT.java"],
    deps = [
        "@graknlabs_client_java//:client-java",
        "@graknlabs_graql//java:graql",
        "//generator:data-generator",
        "//dependencies/maven/artifacts/org/hamcrest:hamcrest-all"
    ],
    classpath_resources = ["//profiler/test-integration/resources:logback-test"],
    data = [
        "//profiler/test-integration/resources:web-content-config"
    ]
)

checkstyle_test(
    name = "checkstyle",
    targets = [
        ":keyspace-schema-labels-it"
    ],
    license_type = "agpl"
)
