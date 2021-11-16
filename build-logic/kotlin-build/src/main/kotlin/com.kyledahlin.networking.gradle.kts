plugins {
    id("java-library")
    id("com.kyledahlin.libraries")
}

// JUnit5 dependencies
dependencies {
    libs.ktorLibs.forEach { dep -> implementation("${dep}:1.6.5") }
}