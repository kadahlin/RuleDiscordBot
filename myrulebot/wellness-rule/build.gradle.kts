plugins {
    id("com.kyledahlin.kotlin")
}

dependencies {

    implementation(libs.firebase)

    implementation("com.kyledahlin.platform:rulebot")
    implementation("com.kyledahlin.platform:discord4k")
    implementation("com.kyledahlin.platform:utils")

    testImplementation("com.kyledahlin.platform:test-utils")
}