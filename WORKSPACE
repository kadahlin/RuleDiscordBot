load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")
load("//third_party:deps.bzl", "TEST", "COMMON", "KTOR_SERVER", "FIREBASE")

rules_kotlin_version = "1.7.1"
rules_kotlin_sha = "fd92a98bd8a8f0e1cdcb490b93f5acef1f1727ed992571232d33de42395ca9b3"
http_archive(
    name = "io_bazel_rules_kotlin",
    urls = ["https://github.com/bazelbuild/rules_kotlin/releases/download/v%s/rules_kotlin_release.tgz" % rules_kotlin_version],
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
    commit = "1492f208ce0ae89310b3d4e1ba63e0cb70f2b4e7"
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
        "https://maven.google.com",
        "https://repo1.maven.org/maven2"
    ],
    override_targets = {
        "org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm": "@//:kotlinx_coroutines_core_jvm",
    }
    #version_conflict_policy = "pinned",
)

# Secondary maven repository used mainly for workarounds
maven_install(
    name = "maven_secondary",
    artifacts = [
        # Workaround to add missing 'sun.misc' dependencies to 'kotlinx-coroutines-core-jvm' artifact
        # Check root BUILD file and 'override_targets' arg of a primary 'maven_install'
        "org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.6.0",
    ],
    fetch_sources = True,
    repositories = ["https://repo1.maven.org/maven2"],
)

## Android

_RULES_ANDROID_VERSION = "0.1.1"

_RULES_ANDROID_SHA = "cd06d15dd8bb59926e4d65f9003bfc20f9da4b2519985c27e190cddc8b7a7806"

http_archive(
    name = "build_bazel_rules_android",
    sha256 = _RULES_ANDROID_SHA,
    strip_prefix = "rules_android-{}".format(_RULES_ANDROID_VERSION),
    urls = [
        "https://github.com/bazelbuild/rules_android/archive/v{}.zip".format(_RULES_ANDROID_VERSION),
    ],
)

load("@build_bazel_rules_android//android:rules.bzl", "android_sdk_repository")

android_sdk_repository(
    name = "androidsdk",
    build_tools_version = "30.0.3"
)