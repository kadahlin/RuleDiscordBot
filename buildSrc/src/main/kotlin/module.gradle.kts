plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    kotlin("kapt")
    jacoco
}

repositories {
    jcenter()
    mavenCentral()
    maven(url = "https://kotlin.bintray.com/kotlinx")
    maven(url = "https://jitpack.io")
}

dependencies {
    implementation("io.arrow-kt:arrow-core:0.13.2")
    //implementation("io.arrow-kt:arrow-syntax:0.13.2")
    kapt("io.arrow-kt:arrow-meta:0.13.2")

    implementation("com.google.dagger:dagger:2.35.1")
    kapt("com.google.dagger:dagger-compiler:2.35.1")

    implementation("org.jetbrains.kotlin:kotlin-stdlib")

    testImplementation("org.junit.jupiter:junit-jupiter:5.5.2")
    testImplementation("org.mockito.kotlin:mockito-kotlin:3.2.0")
    testImplementation("org.assertj:assertj-core:3.11.1")
}

//sourceSets.main.java.srcDirs = ["src/main/kotlin"]
//sourceSets.test.java.srcDirs = ["src/test/kotlin"]

jacoco {
    toolVersion = "0.8.5"
    reportsDir = file("$buildDir/jacocoCoverageReport")
}

val codeCoverageReport by tasks.registering(JacocoReport::class) {
    reports {
        //html.destination = file ("${buildDir}/jacocoHtml")
        xml.isEnabled = true
    }
}

tasks.test {
    useJUnitPlatform()
}

//tasks.check.de(codeCoverageReport)