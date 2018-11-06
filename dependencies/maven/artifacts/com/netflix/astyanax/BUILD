java_library(
    name = "astyanax-cassandra",
    exports = [
        "//external:jar/com/netflix/astyanax/astyanax-cassandra"
    ],
    runtime_deps = [
        "//dependencies/maven/artifacts/commons-codec:commons-codec",
        "//dependencies/maven/artifacts/org/apache/cassandra:cassandra-all",
        "//dependencies/maven/artifacts/org/apache/cassandra:cassandra-thrift",
        "//dependencies/maven/artifacts/org/apache/servicemix/bundles:org-apache-servicemix-bundles-commons-csv",
        "//dependencies/maven/artifacts/org/codehaus/jackson:jackson-mapper-asl",
        "//dependencies/maven/artifacts/org/codehaus/jettison:jettison",
        "//dependencies/maven/artifacts/org/slf4j:slf4j-api",
        "//dependencies/maven/artifacts/org/xerial/snappy:snappy-java",
        ":astyanax-core"
    ],
    visibility = [
        "//dependencies/maven/artifacts:__subpackages__"
    ]
)



java_library(
    name = "astyanax-core",
    exports = [
        "//external:jar/com/netflix/astyanax/astyanax-core"
    ],
    runtime_deps = [
        "//dependencies/maven/artifacts/com/eaio/uuid:uuid",
        "//dependencies/maven/artifacts/com/github/stephenc/high-scale-lib:high-scale-lib",
        "//dependencies/maven/artifacts/com/google/guava:guava",
        "//dependencies/maven/artifacts/commons-lang:commons-lang",
        "//dependencies/maven/artifacts/joda-time:joda-time",
        "//dependencies/maven/artifacts/org/apache/cassandra:cassandra-all",
        "//dependencies/maven/artifacts/org/slf4j:slf4j-api"
    ],
    visibility = [
        "//dependencies/maven/artifacts:__subpackages__"
    ]
)



java_library(
    name = "astyanax-recipes",
    exports = [
        "//external:jar/com/netflix/astyanax/astyanax-recipes"
    ],
    runtime_deps = [
        "//dependencies/maven/artifacts/com/google/guava:guava",
        "//dependencies/maven/artifacts/org/slf4j:slf4j-api",
        ":astyanax-cassandra",
        ":astyanax-core"
    ],
    visibility = [
        "//dependencies/maven/artifacts:__subpackages__"
    ]
)



java_library(
    name = "astyanax-thrift",
    exports = [
        "//external:jar/com/netflix/astyanax/astyanax-thrift"
    ],
    runtime_deps = [
        "//dependencies/maven/artifacts/org/apache/cassandra:cassandra-thrift",
        "//dependencies/maven/artifacts/org/apache/thrift:libthrift",
        ":astyanax-cassandra",
        ":astyanax-core"
    ],
    visibility = [
        "//dependencies/maven/artifacts:__subpackages__"
    ]
)


