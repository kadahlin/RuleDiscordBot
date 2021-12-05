import com.kyledahlin.skrapeIt

plugins {
    application
    id("com.kyledahlin.kotlin")
}

dependencies {

    skrapeIt()
    implementation(libs.kotlinSerialization)
    implementation(libs.firebase)

    libs.ktorServerLibs.forEach { dep -> implementation("${dep}:1.6.5") }

    listOf(
        "rulebot",
        "discord4k",
        "models",
        "utils",
        "wellness-rule",
        "wellness-models"
    ).forEach { implementation("com.kyledahlin.platform:$it") }

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
getTasksByName("run", false)
    .first()
    .dependsOn("generateJks")