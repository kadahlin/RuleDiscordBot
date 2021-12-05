plugins {
    `kotlin-dsl`
    id("com.kyledahlin.libraries")
}

group = "com.kyledahlin.buildlogic"

dependencies {
    implementation("com.kyledahlin.buildlogic:libraries")

    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.31")
    implementation("org.jetbrains.kotlin:kotlin-serialization:1.5.31")
    implementation("com.android.tools.build:gradle:7.0.3")
    implementation("com.google.dagger:hilt-android-gradle-plugin:2.38.1")
}