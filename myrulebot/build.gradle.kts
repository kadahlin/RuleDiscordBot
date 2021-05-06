plugins {
    application
    id("module")
}

repositories {
    maven(url = "https://jitpack.io")
}

dependencies {
    implementation(project(":rulebot"))

    implementation("it.skrape:skrapeit-core:1.0.0-alpha6")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.1")

    //networking (client rules)
    implementation("io.ktor:ktor-client-core:${Dependencies.ktor}")
    implementation("io.ktor:ktor-client-json:${Dependencies.ktor}")
    implementation("io.ktor:ktor-client-json-jvm:${Dependencies.ktor}")
    implementation("io.ktor:ktor-client-apache:${Dependencies.ktor}")
    implementation("io.ktor:ktor-client-serialization-jvm:${Dependencies.ktor}")

    //networking (server instance)
    implementation("io.ktor:ktor-server-core:${Dependencies.ktor}")
    implementation("io.ktor:ktor-server-netty:${Dependencies.ktor}")
    implementation("io.ktor:ktor-serialization:${Dependencies.ktor}")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.1")

    implementation("com.google.firebase:firebase-admin:7.1.1")
}

application {
    mainClassName = "com.kyledahlin.myrulebot.app.MyRuleBotAppKt"
}