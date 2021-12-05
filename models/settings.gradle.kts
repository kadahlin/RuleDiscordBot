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
    repositories.mavenCentral()
}