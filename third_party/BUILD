load("@io_bazel_rules_kotlin//kotlin:core.bzl", "kt_compiler_plugin")
load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library", "kt_jvm_test", "kt_jvm_import")
load("@rules_java//java:defs.bzl", "java_library", "java_plugin")
load("@rules_jvm_external//:defs.bzl", "artifact")

java_plugin(
    name = "dagger_component_plugin",
    generates_api = True,
    processor_class = "dagger.internal.codegen.ComponentProcessor",
    visibility = ["//visibility:private"],
    deps = [
        "@maven//:com_google_dagger_dagger_compiler",
    ],
)

java_library(
    name = "dagger",
    exported_plugins = [":dagger_component_plugin"],
    visibility = ["//visibility:public"],
    exports = [
        "@maven//:com_google_dagger_dagger",
        "@maven//:javax_inject_javax_inject"
    ],
)

kt_compiler_plugin(
    name = "serialization_plugin",
    compile_phase = True,
    id = "org.jetbrains.kotlin.serialization",
    stubs_phase = True,
    deps = [
        "@com_github_jetbrains_kotlin//:kotlinx-serialization-compiler-plugin",
    ],
)

kt_jvm_library(
    name = "common",
    visibility = ["//visibility:public"],
    exported_compiler_plugins = [":serialization_plugin"],
    exports = [
        "@maven//:org_jetbrains_kotlinx_kotlinx_coroutines_core",
        "@maven//:org_jetbrains_kotlinx_kotlinx_coroutines_reactor",
        "@maven//:org_jetbrains_kotlinx_kotlinx_serialization_json",
        "@maven//:io_ktor_ktor_client_core_jvm",
        "@maven//:io_ktor_ktor_client_serialization_jvm",
        "@maven//:io_ktor_ktor_client_json_jvm",
        "@maven//:io_ktor_ktor_client_auth_jvm",
        "@maven//:io_ktor_ktor_client_apache",
        "@maven//:io_ktor_ktor_network_tls_certificates",
        "@maven//:io_ktor_ktor_serialization",
        "@maven//:com_discord4j_discord4j_core",
        "@maven//:io_arrow_kt_arrow_core",
        "@maven//:it_skrape_skrapeit"
    ],
)

kt_jvm_library(
    name = "server",
    visibility = ["//visibility:public"],
    exports = [
        "@maven//:io_ktor_ktor_server_core",
        "@maven//:io_ktor_ktor_server_netty",
    ],
)

kt_jvm_library(
    name = "firebase",
    visibility = ["//visibility:public"],
    exports = [
        "@maven//:com_google_firebase_firebase_admin",
    ],
)

kt_jvm_library(
    name = "test",
    visibility = ["//visibility:public"],
    exports = [
        "@maven//:io_strikt_strikt_core",
        "@maven//:io_mockk_mockk",
        "@maven//:org_junit_jupiter_junit_jupiter"
    ],
)