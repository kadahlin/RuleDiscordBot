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
    includeBuild("../models")
    includeBuild("../utils")
    includeBuild("../wellness-rule")
    includeBuild("../wellness-models")
    includeBuild("../destiny-rule")
    includeBuild("../psycho-rule")
}