load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

def bot_module(name, deps = [], **kwargs):
    kt_jvm_library(
        name = name,
        srcs = native.glob(["src/main/**/*.kt"]),
        resources = native.glob(["src/main/resources/**/*"]),
        visibility = ["//visibility:public"],
        deps = [
            "//third_party:common",
            "//third_party:dagger"
        ] + deps,
    )

def rule_module(name, deps = [], **kwargs):
    bot_module(
        name = name,
        deps = [
            "//models",
            "//discord4k",
            "//rulebot",
            "//utils"
        ] + deps,
    )