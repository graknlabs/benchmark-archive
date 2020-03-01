load("@graknlabs_build_tools//checkstyle:rules.bzl", "checkstyle_test")

java_test(
    name = "configuration-test",
    test_class = "grakn.benchmark.common.configuration.ConfigurationTest",
    srcs = ["ConfigurationTest.java"],
    deps = [
        "@graknlabs_graql//java:graql",
        "//common/configuration:benchmark-configuration",
        "//dependencies/maven/artifacts/commons-cli",
    ],
    data = [
        "//common/configuration/test/resources:web-content-config"
    ]
)

checkstyle_test(
    name = "checkstyle",
    targets = [":configuration-test"],
    license_type = "agpl"
)
