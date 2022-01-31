load("//tools:kotlin.bzl", "bot_module")
load("@rules_java//java:defs.bzl", "java_binary")

bot_module(
    name = "app_module",
    visibility = ["//visibility:private"],
    srcs = glob(["src/main/**/*.kt"]),
    deps = [
        "//third_party:server",
        "//third_party:firebase",
        "//models",
        "//wellness_rule",
        "//destiny_rule",
        "//mtg_rule",
        "//psycho_rule",
    ],
)

java_binary(
    name = "app",
    main_class = "com.kyledahlin.myrulebot.app.MyRuleBotAppKt",
    runtime_deps = [":app_module"],
)