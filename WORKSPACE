load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")
load("//third_party:deps.bzl", "TEST", "COMMON", "KTOR_SERVER", "FIREBASE")

rules_kotlin_version = "v1.5.0-beta-4"
rules_kotlin_sha = "6cbd4e5768bdfae1598662e40272729ec9ece8b7bded8f0d2c81c8ff96dc139d"
http_archive(
    name = "io_bazel_rules_kotlin",
    urls = ["https://github.com/bazelbuild/rules_kotlin/releases/download/%s/rules_kotlin_release.tgz" % rules_kotlin_version],
    sha256 = rules_kotlin_sha,
)

load("@io_bazel_rules_kotlin//kotlin:repositories.bzl", "kotlin_repositories")
kotlin_repositories()

register_toolchains("//:kotlin_toolchain")

http_archive(
    name = "rules_jvm_external",
    sha256 = "cd1a77b7b02e8e008439ca76fd34f5b07aecb8c752961f9640dea15e9e5ba1ca",
    strip_prefix = "rules_jvm_external-4.2",
    url = "https://github.com/bazelbuild/rules_jvm_external/archive/%s.zip" % "4.2",
)

load("@bazel_tools//tools/build_defs/repo:git.bzl", "git_repository")
git_repository(
    name = "skryfall",
    remote = "https://github.com/kadahlin/Skryfall.git",
    branch = "master"
)

load("@rules_jvm_external//:repositories.bzl", "rules_jvm_external_deps")

rules_jvm_external_deps()

load("@rules_jvm_external//:setup.bzl", "rules_jvm_external_setup")

rules_jvm_external_setup()

load("@rules_jvm_external//:defs.bzl", "maven_install")
load("@skryfall//skryfall:skryfall_deps.bzl", "SKRYFALL_ARTIFACTS")
load("@skryfall//skryfall-test:skryfall-test_deps.bzl", "SKRYFALL_TEST_ARTIFACTS")

maven_install(
    artifacts = TEST + COMMON + KTOR_SERVER + FIREBASE + SKRYFALL_ARTIFACTS + SKRYFALL_TEST_ARTIFACTS,
    repositories = [
        "https://repo1.maven.org/maven2",
    ],
)