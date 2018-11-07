java_library(
    name = "ignite-core",
    exports = [
        "//external:jar/org/apache/ignite/ignite-core"
    ],
    runtime_deps = [
        "//dependencies/maven/artifacts/javax/cache:cache-api",
        "//dependencies/maven/artifacts/org/gridgain:ignite-shmem",
        "//dependencies/maven/artifacts/org/jetbrains:annotations"
    ],
    visibility = [
        "//visibility:public"
    ]
)



java_library(
    name = "ignite-indexing",
    exports = [
        "//external:jar/org/apache/ignite/ignite-indexing"
    ],
    runtime_deps = [
        "//dependencies/maven/artifacts/com/h2database:h2",
        "//dependencies/maven/artifacts/commons-codec:commons-codec",
        "//dependencies/maven/artifacts/org/apache/lucene:lucene-analyzers-common",
        "//dependencies/maven/artifacts/org/apache/lucene:lucene-core",
        "//dependencies/maven/artifacts/org/apache/lucene:lucene-queryparser",
        ":ignite-core"
    ],
    visibility = [
        "//visibility:public"
    ]
)


