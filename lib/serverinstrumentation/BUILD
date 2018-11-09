java_library(
    name = "serverinstrumentation",
    srcs = glob(["*.java"]),
    deps = [
        "//lib/util:util",
        "//dependencies/maven/artifacts/ai/grakn:rpc",
        "//dependencies/maven/artifacts/com/google/protobuf:protobuf-java",
        "//dependencies/maven/artifacts/io/zipkin/brave:brave",
        "//dependencies/maven/artifacts/io/zipkin/zipkin2:zipkin",
        "//dependencies/maven/artifacts/io/zipkin/reporter2:zipkin-reporter",
        "//dependencies/maven/artifacts/io/zipkin/reporter2:zipkin-sender-urlconnection"
    ],
    visibility = ["//visibility:public"]
)