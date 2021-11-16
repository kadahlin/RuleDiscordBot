plugins {
    id("java-library")
    kotlin("jvm")
    kotlin("kapt")
    kotlin("plugin.serialization")
    id("com.kyledahlin.libraries")
    id("com.kyledahlin.testing")
    id("com.kyledahlin.networking")
}

group = "com.kyledahlin.platform"

dependencies {
    implementation(libs.discord4j)

    implementation(libs.dagger)
    kapt(libs.daggerCompiler)

    implementation(libs.arrowCore)
    implementation(libs.arrowMeta)

}

repositories {
    mavenCentral()
    maven(url = "https://kotlin.bintray.com/kotlinx")
    maven(url = "https://jitpack.io")
}

// configure details of java compilation
tasks.withType<JavaCompile>().configureEach {
    options.headerOutputDirectory.convention(null) // currently, need to clear convention to remove
}

// Share sources folder with other projects for aggregated Javadoc and JaCoCo reports
configurations.create("transitiveSourcesElements") {
    isVisible = false
    isCanBeResolved = false
    isCanBeConsumed = true
    extendsFrom(configurations.implementation.get())
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.DOCUMENTATION))
        attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named("source-folders"))
    }
    sourceSets.main.get().java.srcDirs.forEach { outgoing.artifact(it) }
}