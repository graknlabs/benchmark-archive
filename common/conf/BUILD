filegroup(name="biochemical-conf", srcs=["biochemical_network/biochemical_config_read.yml"], visibility = ["//visibility:public"])
filegroup(name="biochemical-queries", srcs=["biochemical_network/queries_read.yml"], visibility = ["//visibility:public"])
filegroup(name="biochemical-schema", srcs=["biochemical_network/biochemical_network.gql"], visibility = ["//visibility:public"])

filegroup(name="financial-conf", srcs=["financial_transactions/financial_config_read.yml"], visibility = ["//visibility:public"])
filegroup(name="financial-queries", srcs=["financial_transactions/queries_read.yml"], visibility = ["//visibility:public"])
filegroup(name="financial-schema", srcs=["financial_transactions/financial.gql"], visibility = ["//visibility:public"])

filegroup(name="road-conf-read", srcs=["road_network/road_config_read.yml"], visibility = ["//visibility:public"])
filegroup(name="road-conf-write", srcs=["road_network/road_config_read.yml"], visibility = ["//visibility:public"])
filegroup(name="road-queries-read", srcs=["road_network/queries_read.yml"], visibility = ["//visibility:public"])
filegroup(name="road-queries-write", srcs=["road_network/queries_write.yml"], visibility = ["//visibility:public"])
filegroup(name="road-schema", srcs=["road_network/road_network.gql"], visibility = ["//visibility:public"])

filegroup(name="social-conf", srcs=["social_network/social_network_config_read.yml"], visibility = ["//visibility:public"])
filegroup(name="social-queries", srcs=["social_network/queries_read.yml"], visibility = ["//visibility:public"])
filegroup(name="social-schema", srcs=["social_network/social_network.gql"], visibility = ["//visibility:public"])

filegroup(name="complex-conf-read", srcs=["complex/config_read.yml"], visibility = ["//visibility:public"])
filegroup(name="complex-conf-write", srcs=["complex/config_write.yml"], visibility = ["//visibility:public"])
filegroup(name="complex-queries-read", srcs=["complex/queries_complex_read.yml"], visibility = ["//visibility:public"])
filegroup(name="complex-queries-write", srcs=["complex/queries_complex_write.yml"], visibility = ["//visibility:public"])
filegroup(name="complex-schema", srcs=["complex/schema.gql"], visibility = ["//visibility:public"])


# ---- shortcut filegroups ----


filegroup(
    name = "biochemical-network",
    srcs = [
            "biochemical_network/biochemical_config_read.yml",
            "biochemical_network/biochemical_network.gql",
            "biochemical_network/queries_read.yml",
        ]
        , visibility = ["//visibility:public"]
)

filegroup(
    name = "financial-transactions",
    srcs = [
            "financial_transactions/financial_config_read.yml",
            "financial_transactions/financial.gql",
            "financial_transactions/queries_read.yml"
        ],
    visibility = ["//visibility:public"]
)

filegroup(
    name = "road-network",
    srcs = [
            "road_network/road_config_read.yml",
            "road_network/road_config_write.yml",
            "road_network/queries_read.yml",
            "road_network/queries_write.yml",
            "road_network/road_network.gql"
        ],
    visibility = ["//visibility:public"]
)

filegroup(
    name = "social-network",
    srcs = [
            "social_network/social_network_config_read.yml",
            "social_network/queries_read.yml",
            "social_network/social_network.gql"
        ],
    visibility = ["//visibility:public"]
)

filegroup(
    name = "complex-network",
    srcs = [
            "complex/config_read.yml",
            "complex/config_write.yml",
            "complex/queries_complex_read.yml",
            "complex/queries_complex_write.yml",
            "complex/schema.gql"
        ],
    visibility = ["//visibility:public"]
)

