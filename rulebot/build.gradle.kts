plugins {
    id("module")
}

dependencies {
    api(project(":discord4k"))

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.3.1")

    implementation("io.ktor:ktor-client-core:${Dependencies.ktor}")
    implementation("io.ktor:ktor-client-json:${Dependencies.ktor}")
    implementation("io.ktor:ktor-client-json-jvm:${Dependencies.ktor}")
    implementation("io.ktor:ktor-client-apache:${Dependencies.ktor}")
    implementation("io.ktor:ktor-client-serialization-jvm:${Dependencies.ktor}")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.1")
}