#load("@build_bazel_rules_android//android:rules.bzl", "android_binary")
#load("@io_bazel_rules_kotlin//kotlin:android.bzl", "kt_android_library")
#
## An app that consumes android-kt deps
#android_binary(
#    name = "rulebot_app",
#    custom_package = "com.kyledahlin.rulebot.config",
#    dex_shards = 1,
#    manifest = "src/main/AndroidManifest.xml",
#    multidex = "native",
#    visibility = ["//visibility:public"],
#    deps = [
#        ":lib",
#    ],
#)
#
#kt_android_library(
#    name = "lib",
#    srcs = glob(["src/main/kotlin/**/*.kt"]),
#    custom_package = "com.kyledahlin.rulebot.config",
#    manifest = "src/main/AndroidManifest.xml",
#    plugins = ["//:jetpack_compose_compiler_plugin"],
#    visibility = ["//visibility:public"],
#    deps = [
#        "@maven//:androidx_activity_activity_compose",
#        "@maven//:androidx_appcompat_appcompat",
#        "@maven//:androidx_compose_material_material",
#        "@maven//:androidx_compose_runtime_runtime",
#        "@maven//:androidx_compose_ui_ui",
#        "@maven//:androidx_compose_ui_ui_tooling",
#        "@maven//:androidx_core_core_ktx",
#        "@maven//:org_jetbrains_kotlin_kotlin_stdlib",
#    ],
#)