plugins {
    application
    id("com.kyledahlin.kotlin")
}

dependencies {

    implementation("${libs.skrapeit}:1.0.0-alpha6") {
        exclude(group = "io.strikt", module = "strikt-core")
    }

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.0")

    implementation(libs.firebase)

    implementation("com.kyledahlin.platform:rulebot")
    implementation("com.kyledahlin.platform:discord4k")
    implementation("com.kyledahlin.platform:utils")
    implementation("com.kyledahlin.platform:wellness-rule")

    testImplementation("com.kyledahlin.platform:test-utils")
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