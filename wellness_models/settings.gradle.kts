pluginManagement {
    includeBuild("../repositories")

    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories.mavenCentral()
}

plugins {
    id("com.kyledahlin.repositories")
}