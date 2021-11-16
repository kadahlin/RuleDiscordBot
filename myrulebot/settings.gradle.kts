pluginManagement {
    includeBuild("../repositories")
}

plugins {
    id("com.kyledahlin.repositories")
}

dependencyResolutionManagement {
    includeBuild("../rulebot")
    includeBuild("../discord4k")
}

include("myrulebot")
include("wellness-rule")
include("utils")
include("test-utils")
