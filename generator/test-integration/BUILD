java_test(
    name = "keyspace-schema-labels-it",
    test_class = "grakn.benchmark.generator.util.KeyspaceSchemaLabelsIT",
    srcs = ["util/KeyspaceSchemaLabelsIT.java"],
    deps = [
        "@graknlabs_client_java//:client-java",
        "@graknlabs_grakn_core//concept:concept",
        "@graknlabs_graql//java:graql",
        "//generator:data-generator",
        "//dependencies/maven/artifacts/org/hamcrest:hamcrest-all"
    ],
    classpath_resources = ["//profiler/test-integration/resources:logback-test"],
    data = [
        "//profiler/test-integration/resources:web-content-config"
    ]
)