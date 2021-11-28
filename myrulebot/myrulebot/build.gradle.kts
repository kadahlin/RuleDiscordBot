import com.kyledahlin.skrapeIt

plugins {
    application
    id("com.kyledahlin.kotlin")
}

dependencies {

    skrapeIt()
    implementation(libs.kotlinSerialization)
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
getTasksByName("run", false)
    .first()
    .dependsOn("generateJks")