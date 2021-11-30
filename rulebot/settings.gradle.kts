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
    includeBuild("../discord4k")
    includeBuild("../models")
}

include("rulebot")