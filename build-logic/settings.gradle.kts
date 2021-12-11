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

    includeBuild("../libraries")

    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

include("kotlin-build")
include("lifecycle")