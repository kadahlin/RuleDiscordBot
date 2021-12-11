pluginManagement {
    includeBuild("../repositories")

    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("com.kyledahlin.repositories")
}

dependencyResolutionManagement {
    includeBuild("../rulebot")
    includeBuild("../discord4k")
    includeBuild("../test-utils")
    includeBuild("../utils")
}

