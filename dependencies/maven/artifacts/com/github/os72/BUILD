java_library(
    name = "protobuf-java-shaded-351",
    exports = [
        "//external:jar/com/github/os72/protobuf-java-shaded-351"
    ],
    visibility = [
        "//dependencies/maven/artifacts:__subpackages__"
    ]
)



java_library(
    name = "protobuf-java-util-shaded-351",
    exports = [
        "//external:jar/com/github/os72/protobuf-java-util-shaded-351"
    ],
    runtime_deps = [
        "//dependencies/maven/artifacts/com/google/code/gson:gson",
        "//dependencies/maven/artifacts/com/google/guava:guava",
        ":protobuf-java-shaded-351"
    ],
    visibility = [
        "//dependencies/maven/artifacts:__subpackages__"
    ]
)


