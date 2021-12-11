plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    kotlin("plugin.serialization")
    id("com.kyledahlin.libraries")
    id("dagger.hilt.android.plugin")
}

dependencies {
    implementation("com.google.dagger:hilt-android:2.38.1")
    kapt("com.google.dagger:hilt-android-compiler:2.38.1")

    implementation(libs.arrowCore)
    implementation(libs.arrowMeta)

    implementation(libs.kotlinSerialization)
}

group = "com.kyledahlin.platform"

repositories {
    mavenCentral()
    google()
    maven(url = "https://kotlin.bintray.com/kotlinx")
    maven(url = "https://jitpack.io")
}

kapt {
    correctErrorTypes = true
}