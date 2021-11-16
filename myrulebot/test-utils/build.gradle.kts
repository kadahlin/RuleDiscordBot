plugins {
    id("com.kyledahlin.kotlin")
}

dependencies {
    implementation(libs.strikt)
    implementation(libs.mockk)
    implementation(libs.junit)
    implementation("com.kyledahlin.platform:discord4k")
    implementation("com.kyledahlin.platform:rulebot")
}