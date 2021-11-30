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
    includeBuild("../models")
    includeBuild("../wellness-models")

    repositories.mavenCentral()
}

include("app")