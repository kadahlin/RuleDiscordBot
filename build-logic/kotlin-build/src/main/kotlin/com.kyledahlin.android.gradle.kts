plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    kotlin("plugin.serialization")
    id("com.kyledahlin.libraries")
}

group = "com.kyledahlin.platform"

repositories {
    mavenCentral()
    google()
    maven(url = "https://kotlin.bintray.com/kotlinx")
    maven(url = "https://jitpack.io")
}