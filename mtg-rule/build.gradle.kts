plugins {
    id("com.kyledahlin.kotlin")
}

dependencies {

    implementation(libs.firebase)

    implementation("com.kyledahlin.platform:rulebot")
    implementation("com.kyledahlin.platform:discord4k")
    implementation("com.kyledahlin.platform:utils")

    implementation("com.kyledahlin:skryfall-coroutines:0.8.0")

    testImplementation("com.kyledahlin.platform:test-utils")
}