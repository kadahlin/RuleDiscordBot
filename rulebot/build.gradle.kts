plugins {
    id("module")
}

dependencies {
    api(project(":discord4k"))

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.0")

    implementation("io.ktor:ktor-client-core:${Dependencies.ktor}")
    implementation("io.ktor:ktor-client-json:${Dependencies.ktor}")
    implementation("io.ktor:ktor-client-json-jvm:${Dependencies.ktor}")
    implementation("io.ktor:ktor-client-apache:${Dependencies.ktor}")
    implementation("io.ktor:ktor-client-serialization-jvm:${Dependencies.ktor}")
}