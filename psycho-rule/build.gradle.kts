plugins {
    id("com.kyledahlin.kotlin")
}

dependencies {

    implementation("com.kyledahlin.platform:rulebot")
    implementation("com.kyledahlin.platform:discord4k")
    implementation("com.kyledahlin.platform:utils")
    implementation("com.kyledahlin.platform:models")

    testImplementation("com.kyledahlin.platform:test-utils")
}