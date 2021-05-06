pluginManagement {
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "kotlinx-serialization") {
                useModule("org.jetbrains.kotlin:kotlin-serialization:${requested.version}")
            }
        }
    }
}

include(":discord4k")
include(":rulebot")

rootProject.name = "rulebot"
include("myrulebot")
include("rulebotanalytics")