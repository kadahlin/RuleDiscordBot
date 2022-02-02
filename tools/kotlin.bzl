load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library", "kt_jvm_test")

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

def bot_test(name, deps = [], **kwargs):
    kt_jvm_test(
        name = name,
        visibility = ["//visibility:private"],
        srcs = native.glob(["src/**/*.kt"]),
        args = [
            "--select-package=com.kyledahlin",
        ],
        main_class = "org.junit.platform.console.ConsoleLauncher",
        deps = [
            "//test_utils",
            "//third_party:test",
            "//third_party:common",
            "//third_party:dagger"
        ] + deps
    )

def rule_test(name, deps = [], **kwargs):
    bot_test(
        name = name,
        deps = [
            "//models",
            "//discord4k",
            "//rulebot",
            "//utils",
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