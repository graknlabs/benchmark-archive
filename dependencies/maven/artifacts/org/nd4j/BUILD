java_library(
    name = "jackson",
    exports = [
        "//external:jar/org/nd4j/jackson"
    ],
    runtime_deps = [
        "//dependencies/maven/artifacts/joda-time:joda-time",
        "//dependencies/maven/artifacts/org/codehaus/woodstox:stax2-api",
        "//dependencies/maven/artifacts/org/yaml:snakeyaml"
    ],
    visibility = [
        "//dependencies/maven/artifacts:__subpackages__"
    ]
)



java_library(
    name = "nd4j-api",
    exports = [
        "//external:jar/org/nd4j/nd4j-api"
    ],
    runtime_deps = [
        "//dependencies/maven/artifacts/com/github/os72:protobuf-java-shaded-351",
        "//dependencies/maven/artifacts/com/github/os72:protobuf-java-util-shaded-351",
        "//dependencies/maven/artifacts/com/google/flatbuffers:flatbuffers-java",
        "//dependencies/maven/artifacts/commons-net:commons-net",
        "//dependencies/maven/artifacts/net/ericaro:neoitertools",
        "//dependencies/maven/artifacts/org/apache/commons:commons-math3",
        "//dependencies/maven/artifacts/org/objenesis:objenesis",
        "//dependencies/maven/artifacts/org/slf4j:slf4j-api",
        "//dependencies/maven/artifacts/uk/com/robust-it:cloning",
        ":jackson",
        ":nd4j-buffer",
        ":nd4j-context"
    ],
    visibility = [
        "//visibility:public"
    ]
)



java_library(
    name = "nd4j-buffer",
    exports = [
        "//external:jar/org/nd4j/nd4j-buffer"
    ],
    runtime_deps = [
        "//dependencies/maven/artifacts/org/bytedeco:javacpp",
        ":nd4j-context"
    ],
    visibility = [
        "//dependencies/maven/artifacts:__subpackages__"
    ]
)



java_library(
    name = "nd4j-common",
    exports = [
        "//external:jar/org/nd4j/nd4j-common"
    ],
    runtime_deps = [
        "//dependencies/maven/artifacts/com/google/guava:guava",
        "//dependencies/maven/artifacts/commons-codec:commons-codec",
        "//dependencies/maven/artifacts/commons-io:commons-io",
        "//dependencies/maven/artifacts/org/apache/commons:commons-compress",
        "//dependencies/maven/artifacts/org/apache/commons:commons-lang3",
        "//dependencies/maven/artifacts/org/apache/commons:commons-math3",
        "//dependencies/maven/artifacts/org/slf4j:slf4j-api",
        ":jackson"
    ],
    visibility = [
        "//dependencies/maven/artifacts:__subpackages__"
    ]
)



java_library(
    name = "nd4j-context",
    exports = [
        "//external:jar/org/nd4j/nd4j-context"
    ],
    runtime_deps = [
        ":nd4j-common"
    ],
    visibility = [
        "//dependencies/maven/artifacts:__subpackages__"
    ]
)



java_library(
    name = "nd4j-native",
    exports = [
        "//external:jar/org/nd4j/nd4j-native"
    ],
    runtime_deps = [
        "//dependencies/maven/artifacts/org/bytedeco/javacpp-presets:mkl",
        "//dependencies/maven/artifacts/org/bytedeco/javacpp-presets:mkl-dnn",
        "//dependencies/maven/artifacts/org/bytedeco/javacpp-presets:mkl-dnn-jar-macosx-x86-64",
        "//dependencies/maven/artifacts/org/bytedeco/javacpp-presets:mkl-jar-macosx-x86-64",
        "//dependencies/maven/artifacts/org/bytedeco/javacpp-presets:openblas",
        "//dependencies/maven/artifacts/org/bytedeco/javacpp-presets:openblas-jar-macosx-x86-64",
        "//dependencies/maven/artifacts/org/bytedeco:javacpp",
        ":nd4j-api",
        ":nd4j-native-api"
    ],
    visibility = [
        "//visibility:public"
    ]
)



java_library(
    name = "nd4j-native-api",
    exports = [
        "//external:jar/org/nd4j/nd4j-native-api"
    ],
    runtime_deps = [
        "//dependencies/maven/artifacts/org/bytedeco:javacpp",
        ":nd4j-api",
        ":nd4j-buffer"
    ],
    visibility = [
        "//visibility:public"
    ]
)



java_library(
    name = "nd4j-native-jar-linux-x86-64",
    exports = [
        "//external:jar/org/nd4j/nd4j-native-jar-linux-x86-64"
    ],
    runtime_deps = [
        "//dependencies/maven/artifacts/org/bytedeco/javacpp-presets:mkl",
        "//dependencies/maven/artifacts/org/bytedeco/javacpp-presets:mkl-dnn",
        "//dependencies/maven/artifacts/org/bytedeco/javacpp-presets:mkl-dnn-jar-macosx-x86-64",
        "//dependencies/maven/artifacts/org/bytedeco/javacpp-presets:mkl-jar-macosx-x86-64",
        "//dependencies/maven/artifacts/org/bytedeco/javacpp-presets:openblas",
        "//dependencies/maven/artifacts/org/bytedeco/javacpp-presets:openblas-jar-macosx-x86-64",
        "//dependencies/maven/artifacts/org/bytedeco:javacpp",
        ":nd4j-api",
        ":nd4j-native-api"
    ],
    visibility = [
        "//visibility:public"
    ]
)



java_library(
    name = "nd4j-native-jar-macosx-x86-64",
    exports = [
        "//external:jar/org/nd4j/nd4j-native-jar-macosx-x86-64"
    ],
    runtime_deps = [
        "//dependencies/maven/artifacts/org/bytedeco/javacpp-presets:mkl",
        "//dependencies/maven/artifacts/org/bytedeco/javacpp-presets:mkl-dnn",
        "//dependencies/maven/artifacts/org/bytedeco/javacpp-presets:mkl-dnn-jar-macosx-x86-64",
        "//dependencies/maven/artifacts/org/bytedeco/javacpp-presets:mkl-jar-macosx-x86-64",
        "//dependencies/maven/artifacts/org/bytedeco/javacpp-presets:openblas",
        "//dependencies/maven/artifacts/org/bytedeco/javacpp-presets:openblas-jar-macosx-x86-64",
        "//dependencies/maven/artifacts/org/bytedeco:javacpp",
        ":nd4j-api",
        ":nd4j-native-api"
    ],
    visibility = [
        "//visibility:public"
    ]
)


