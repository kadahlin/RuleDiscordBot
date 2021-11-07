package com.kyledahlin.myrulebot.app

import io.ktor.application.*
import io.ktor.network.tls.certificates.*
import io.ktor.network.tls.extensions.*
import io.ktor.server.engine.*
import java.io.File
import java.security.KeyStore

private const val keyAlias = "mykey"
private const val keyPass = "changeit"

fun ApplicationEngineEnvironmentBuilder.applySelfCert(
    host: String,
    port: Int,
    module: Application.() -> Unit
) {

    val keystore = buildKeyStore {
        certificate(keyAlias) {
            hash = HashAlgorithm.SHA256
            sign = SignatureAlgorithm.ECDSA
            keySizeInBits = 256
            password = keyPass
        }
        // More certificates come here
    }

    sslConnector(keystore, keyAlias, { keyPass.toCharArray() }, { keyPass.toCharArray() }) {
        this.host = host
        this.port = port
        keyStorePath = keystore.asFile.absoluteFile

        module(module)
    }
}

private val KeyStore.asFile: File
    get() {
        val keyStoreFile = File("build/temporary.jks")
        this.saveToFile(keyStoreFile, keyPass)
        return keyStoreFile
    }