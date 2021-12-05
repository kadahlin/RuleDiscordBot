package com.kyledahlin.rulebot.config

import com.kyledahlin.models.Response
import okhttp3.OkHttpClient
import retrofit2.http.GET
import retrofit2.http.Path
import java.security.SecureRandom
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

interface ConfigService {
    @GET("rules")
    suspend fun getRules(): Response

    @GET("guilds")
    suspend fun getGuilds(): Response

    @GET("guilds/{guildId}")
    suspend fun getGuildInfo(@Path("guildId") guildId: String): Response
}

fun createOkHttpClient(): OkHttpClient {
    val sslContext = SSLContext.getInstance("SSL")
    sslContext.init(null, arrayOf(AllowAllSslManager()), SecureRandom())
    return OkHttpClient.Builder()
        .sslSocketFactory(sslContext.socketFactory, AllowAllSslManager())
        .hostnameVerifier { _, _ -> true }
        .build()
}

class AllowAllSslManager : X509TrustManager {

    override fun checkServerTrusted(
        p0: Array<out java.security.cert.X509Certificate>?,
        p1: String?
    ) {
        //allow all
    }

    override fun checkClientTrusted(
        p0: Array<out java.security.cert.X509Certificate>?,
        p1: String?
    ) {
        //allow all
    }

    override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> = arrayOf()
}