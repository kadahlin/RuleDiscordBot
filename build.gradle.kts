buildscript {
    repositories {
        mavenCentral()
        maven (url = "https://kotlin.bintray.com/kotlinx")
        jcenter()
    }

    dependencies {
        classpath(kotlin("gradle-plugin", version = Dependencies.kotlin))
        classpath(kotlin("serialization", version = Dependencies.kotlin))
    }
}

allprojects {
    repositories {
        mavenCentral()
        maven(url = "https://kotlin.bintray.com/kotlinx")
        jcenter()
    }
}