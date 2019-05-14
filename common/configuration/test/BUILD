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
