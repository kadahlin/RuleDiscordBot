package com.kyledahlin

import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.add
import org.gradle.kotlin.dsl.exclude

abstract class ExternalLibrariesExtension {
    private val daggerBase = "com.google.dagger:dagger"
    private val daggerVersion = "2.35.1"
    private val arrowBase = "io.arrow-kt:arrow"

    private val coroutinesBase = "org.jetbrains.kotlinx:kotlinx-coroutines"
    private val coroutinesVersion = "1.5.2"

    val coroutinesCore = "${coroutinesBase}-core:$coroutinesVersion"
    val coroutinesReactor = "${coroutinesBase}-reactor:$coroutinesVersion"

    val discord4j = "com.discord4j:discord4j-core:3.2.0"

    val dagger = "$daggerBase:$daggerVersion"
    val daggerCompiler = "${daggerBase}-compiler:$daggerVersion"

    private val arrowVersion = "1.0.1"
    val arrowCore = "${arrowBase}-core:$arrowVersion"
    val arrowMeta = "${arrowBase}-meta:$arrowVersion"

    val junit = "org.junit.jupiter:junit-jupiter:5.5.2"
    val mockk = "io.mockk:mockk:1.12.0"
    val strikt = "io.strikt:strikt-core:0.32.0"

    val skrapeit = ""

    val firebase = "com.google.firebase:firebase-admin:8.1.0"

    val kotlinSerialization = "org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.0"

    private val ktorBase = "io.ktor:ktor"
    private val ktorServerBase = "$ktorBase-server"
    private val ktorClientBase = "$ktorBase-client"

    //networking (client rules)
    val ktorLibs = listOf(
        "$ktorClientBase-core",
        "$ktorClientBase-json",
        "$ktorClientBase-json-jvm",
        "$ktorClientBase-apache",
        "$ktorClientBase-serialization-jvm",
        "$ktorServerBase-core",
        "$ktorServerBase-netty",
        "$ktorBase-serialization",
        "$ktorBase-network-tls-certificates"
    )

    val ktorTest = "${ktorServerBase}-test-host"

    val logback = "ch.qos.logback:logback-classic:1.2.7"
}

fun DependencyHandlerScope.skrapeIt() {
    dependencies.add("implementation", "it.skrape:skrapeit-core:1.0.0-alpha6") {
        exclude(group = "io.strikt", module = "strikt-core")
    }
}