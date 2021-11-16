pluginManagement {
    includeBuild("../repositories")
}

plugins {
    id("com.kyledahlin.repositories")
}

dependencyResolutionManagement {
    includeBuild("../discord4k")
}

include("rulebot")