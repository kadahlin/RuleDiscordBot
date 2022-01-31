package com.kyledahlin.destiny

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.kyledahlin.rulebot.bot.Logger
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.features.auth.*
import io.ktor.client.features.auth.providers.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject

@Serializable
data class TokenInfo(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String? = null,
)

/**
 * Provide HTTP clients that handle OAuth2 management with the Destiny 2 servers.
 */
open class Destiny2ClientProvider @Inject constructor(private val _storage: Destiny2Storage) {

    private val tokenClient = HttpClient(Apache) {
        install(JsonFeature) {
            serializer = KotlinxSerializer(kotlinx.serialization.json.Json {
                ignoreUnknownKeys = true
            })
        }
    }

    open fun getOauthClient(): HttpClient {
        return HttpClient(Apache) {
            install(Auth) {
                lateinit var tokenInfo: TokenInfo
                var refreshTokenInfo: TokenInfo

                bearer {
                    loadTokens {
                        val savedValues = _storage.getAccessAndRefreshToken()
                        tokenInfo = TokenInfo(savedValues.first, savedValues.second)
                        BearerTokens(
                            accessToken = tokenInfo.accessToken, refreshToken = tokenInfo.refreshToken!!
                        )
                    }
                    refreshTokens { unauthorizedResponse: HttpResponse ->
                        refreshTokenInfo =
                            tokenClient.submitForm(url = "https://www.bungie.net/Platform/App/OAuth/token/",
                                formParameters = Parameters.build {
                                    append("grant_type", "refresh_token")
                                    append("client_id", _storage.getClientId())
                                    append("refresh_token", tokenInfo.refreshToken!!)
                                    append("client_secret", _storage.getClientSecret())
                                })
                        _storage.setRefreshAndAccessToken(refreshTokenInfo.accessToken, tokenInfo.refreshToken!!)
                        BearerTokens(
                            accessToken = refreshTokenInfo.accessToken, refreshToken = tokenInfo.refreshToken!!
                        )
                    }
                }
            }
        }
    }
}

private const val BUNGIE_API_ROOT = "https://www.bungie.net"
private const val DESTINY_2_API_ROOT = "$BUNGIE_API_ROOT/Platform/Destiny2"
private const val MEMBERSHIP_ID = "4611686018510393840"
private const val CHARACTER_ID = "2305843009740574188"

private const val ADA_VENDOR_ID = "350061650"

private const val ITEM_DEFINITION = "/Manifest/DestinyInventoryItemDefinition"

/**
 * https://github.com/Bungie-net/api
 */
open class Destiny2Api @Inject constructor(
    clientProvider: Destiny2ClientProvider,
    private val _storage: Destiny2Storage,
    private val _translator: Destiny2Translator
) {

    private val client = clientProvider.getOauthClient()

    private var _apiKey: String? = null

    private suspend fun apiKey(): String {
        if (_apiKey == null) {
            _apiKey = _storage.getApiKey()
        }
        return _apiKey!!
    }

    open suspend fun getTodaysModInformation(): Either<String, List<Pair<String, String>>> {
        val adaResponse =
            client.getWithKey(
                "$DESTINY_2_API_ROOT/3/Profile/$MEMBERSHIP_ID/Character/$CHARACTER_ID/Vendors/$ADA_VENDOR_ID/?components=402"
            )
        Logger.logDebug { "api response = $adaResponse" }

        val modIds = _translator.getModHashesFromResponse(adaResponse)
        if (modIds.isEmpty()) {
            return "Empty mods".left()
        }

        val namesAndUrls = modIds.map {
            val response = client.getWithKey("$DESTINY_2_API_ROOT$ITEM_DEFINITION/$it")
            val contentPath = _translator.getNameAndImageForModResponse(response)
            contentPath.second to "$BUNGIE_API_ROOT${contentPath.first}"
        }
        return namesAndUrls.right()
    }

    private suspend fun HttpClient.getWithKey(urlString: String, block: HttpRequestBuilder.() -> Unit = {}): String {
        return get(urlString) {
            header("X-Api-Key", apiKey())
            block()
        }
    }
}