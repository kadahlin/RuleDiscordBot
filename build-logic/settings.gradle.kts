rootProject.name = "build-logic"

pluginManagement {
    includeBuild("../libraries")

    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories.mavenCentral()

    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

include("kotlin-build")
include("lifecycle")