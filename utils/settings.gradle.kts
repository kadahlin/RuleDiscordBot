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
}

plugins {
    id("com.kyledahlin.repositories")
}