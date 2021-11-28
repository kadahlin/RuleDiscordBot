package com.kyledahlin.myrulebot.app

import io.ktor.application.*
import io.ktor.network.tls.certificates.*
import io.ktor.server.engine.*
import org.slf4j.LoggerFactory
import java.io.File
import java.security.KeyStore

private const val keyAlias = "mykey"
private const val keyPass = "changeit"

fun ApplicationEngineEnvironmentBuilder.applySelfCert(
    host: String,
    port: Int,
    module: Application.() -> Unit
) {

//    log = LoggerFactory.getLogger("ktor.application")

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
        this.port = 9999
        keyStorePath = keystoreFile
    }
    module(module)
}