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
    includeBuild("../utils")
    includeBuild("../test-utils")
}

plugins {
    id("com.kyledahlin.repositories")
}