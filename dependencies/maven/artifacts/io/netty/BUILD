java_library(
    name = "netty",
    exports = [
        "//external:jar/io/netty/netty"
    ],
    visibility = [
        "//dependencies/maven/artifacts:__subpackages__"
    ]
)



java_library(
    name = "netty-all",
    exports = [
        "//external:jar/io/netty/netty-all"
    ],
    visibility = [
        "//dependencies/maven/artifacts:__subpackages__"
    ]
)



java_library(
    name = "netty-buffer",
    exports = [
        "//external:jar/io/netty/netty-buffer"
    ],
    runtime_deps = [
        ":netty-common"
    ],
    visibility = [
        "//dependencies/maven/artifacts:__subpackages__"
    ]
)



java_library(
    name = "netty-codec",
    exports = [
        "//external:jar/io/netty/netty-codec"
    ],
    runtime_deps = [
        ":netty-transport"
    ],
    visibility = [
        "//dependencies/maven/artifacts:__subpackages__"
    ]
)



java_library(
    name = "netty-common",
    exports = [
        "//external:jar/io/netty/netty-common"
    ],
    visibility = [
        "//dependencies/maven/artifacts:__subpackages__"
    ]
)



java_library(
    name = "netty-handler",
    exports = [
        "//external:jar/io/netty/netty-handler"
    ],
    runtime_deps = [
        ":netty-buffer",
        ":netty-codec",
        ":netty-transport"
    ],
    visibility = [
        "//dependencies/maven/artifacts:__subpackages__"
    ]
)



java_library(
    name = "netty-transport",
    exports = [
        "//external:jar/io/netty/netty-transport"
    ],
    runtime_deps = [
        ":netty-buffer"
    ],
    visibility = [
        "//dependencies/maven/artifacts:__subpackages__"
    ]
)


