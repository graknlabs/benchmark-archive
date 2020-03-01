load("@graknlabs_build_tools//checkstyle:rules.bzl", "checkstyle_test")

java_library(
    name = "benchmark-exception",
    srcs = glob(["*.java"]),
    deps = [
    ],
    visibility = ["//visibility:public"]
)

checkstyle_test(
    name = "checkstyle",
    targets = [
        ":benchmark-exception",
    ],
    license_type = "agpl"
)
