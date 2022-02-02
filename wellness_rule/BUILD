load("//tools:kotlin.bzl", "rule_module", "rule_test")

rule_module(
    name = "wellness_rule",
    deps = [
        "//wellness_models"
    ]
)

rule_test(
    name = "test",
    deps = [
        "//wellness_rule",
        "//wellness_models"
    ]
)