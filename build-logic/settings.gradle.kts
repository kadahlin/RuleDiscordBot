rootProject.name = "build-logic"

pluginManagement {
    includeBuild("../libraries")
}
dependencyResolutionManagement {
    repositories.mavenCentral()
}

include("kotlin-build")
include("lifecycle")