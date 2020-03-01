load("@graknlabs_build_tools//checkstyle:rules.bzl", "checkstyle_test")

java_test(
    name = "insert-query-analysis-unit-test",
    test_class = "grakn.benchmark.common.analysis.InsertQueryAnalyserTest",
    srcs = ["InsertQueryAnalyserTest.java"],
    deps = [
        "//common/analysis:insert-query-analyser",
        "@graknlabs_client_java//:client-java",

        "@graknlabs_graql//java:graql",
        "//dependencies/maven/artifacts/org/mockito:mockito-core",
        "//dependencies/maven/artifacts/org/hamcrest:hamcrest-all"
    ]
)

checkstyle_test(
    name = "checkstyle",
    targets = [
        ":insert-query-analysis-unit-test"
    ],
    license_type = "agpl"
)
