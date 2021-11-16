plugins {
    id("java-library")
    id("com.kyledahlin.libraries")
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    testImplementation(libs.mockk)
    testImplementation(libs.strikt)
    testImplementation(libs.junit)
}