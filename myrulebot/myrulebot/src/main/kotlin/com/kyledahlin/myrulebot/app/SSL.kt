package com.kyledahlin.myrulebot.app

import io.ktor.application.*
import io.ktor.network.tls.certificates.*
import io.ktor.server.engine.*
import java.io.File

private const val keyAlias = "mykey"
private const val keyPass = "changeit"

fun ApplicationEngineEnvironmentBuilder.applySelfCert(
    host: String,
    port: Int,
    module: Application.() -> Unit
) {

    val keystoreFile = File("keystore.jks")

    val keystore = generateCertificate(
        file = keystoreFile,
        keyAlias = keyAlias,
        keyPassword = keyPass,
        jksPassword = keyPass
    )

    connector {
        this.port = port
    }

    sslConnector(
        keystore,
        keyAlias,
        { keyPass.toCharArray() },
        { keyPass.toCharArray() },
    ) {
        this.host = host
        this.port = port + 1
        keyStorePath = keystoreFile
    }
    module(module)
}