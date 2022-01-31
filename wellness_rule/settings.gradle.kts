pluginManagement {
    includeBuild("../repositories")

    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    includeBuild("../rulebot")
    includeBuild("../discord4k")
    includeBuild("../models")
    includeBuild("../wellness-models")
    includeBuild("../test-utils")
}

plugins {
    id("com.kyledahlin.repositories")
}