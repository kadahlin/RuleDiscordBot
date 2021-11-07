plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    kotlin("kapt")
    jacoco
}

repositories {
    mavenCentral()
    maven(url = "https://kotlin.bintray.com/kotlinx")
    maven(url = "https://jitpack.io")
}

dependencies {
    implementation("io.arrow-kt:arrow-core:${Dependencies.arrow}")
    kapt("io.arrow-kt:arrow-meta:${Dependencies.arrow}")

    implementation("com.google.dagger:dagger:2.35.1")
    kapt("com.google.dagger:dagger-compiler:2.35.1")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Dependencies.coroutines}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:${Dependencies.coroutines}")

    implementation("org.jetbrains.kotlin:kotlin-stdlib")

    testImplementation("org.junit.jupiter:junit-jupiter:5.5.2")
    testImplementation("io.mockk:mockk:1.12.0")
    testImplementation("io.strikt:strikt-core:0.32.0")
}

jacoco {
    toolVersion = "0.8.5"
    reportsDir = file("$buildDir/jacocoCoverageReport")
}

val codeCoverageReport by tasks.registering(JacocoReport::class) {
    reports {
        xml.isEnabled = true
    }
}

tasks.test {
    useJUnitPlatform()
}