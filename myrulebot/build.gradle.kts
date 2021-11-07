plugins {
    application
    id("module")
}

repositories {
    maven(url = "https://jitpack.io")
}

dependencies {
    implementation(project(":rulebot"))

    implementation("it.skrape:skrapeit-core:1.0.0-alpha6") {
        exclude(group = "io.strikt", module = "strikt-core")
    }
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.0")

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
    implementation("io.ktor:ktor-network-tls-certificates:${Dependencies.ktor}")

    implementation("com.google.firebase:firebase-admin:8.1.0")
}

application {
    mainClassName = "com.kyledahlin.myrulebot.app.MyRuleBotAppKt"
}

tasks.register<JavaExec>("generateJks") {
    dependsOn("classes")
    classpath = sourceSets["main"].runtimeClasspath
    main = "com.kyledahlin.myrulebot.app.CertificateGenerator"
}
getTasksByName("run", false).first().dependsOn("generateJks")