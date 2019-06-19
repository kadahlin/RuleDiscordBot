package bot.twitch

import bot.Logger
import bot.getTokenFromFile
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.get
import io.ktor.client.request.header
import kotlinx.serialization.Optional
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Communicate with the twitch backend and fetch data using the 'new twitch api'
 */
internal interface TwitchBackend {
    suspend fun getIdForUsername(username: String): String?
    suspend fun isUserLive(userId: String): Boolean
}

private const val USERNAME = "<username>"
private const val userIdRequest =
    "https://api.twitch.tv/helix/users?login=$USERNAME"

private const val USER_ID = "<user_id>"
private const val isLiveRequest = "https://api.twitch.tv/helix/streams?user_id=$USER_ID"

@Serializable
private data class TwitchResponse(val data: List<TwitchUserData>)

@Serializable
private data class TwitchUserData(
    @Optional val id: String = "",
    @Optional val login: String = "",
    @Optional @SerialName(value = "display_name") val displayName: String = ""
)

internal class TwitchBackendImpl : TwitchBackend {

    private val twitchClientId by lazy {
        getTokenFromFile("twitchtoken.txt")
    }

    override suspend fun getIdForUsername(username: String): String? {
        val response = client.get<TwitchResponse>(userIdRequest.replace(USERNAME, username)) {
            header("Client-ID", twitchClientId)
        }

        return if (response.data.isEmpty()) {
            null
        } else {
            val userId = response.data.first().id
            Logger.logDebug("id for $username is $userId")
            userId
        }
    }

    override suspend fun isUserLive(userId: String): Boolean {
        val response = client.get<TwitchResponse>(isLiveRequest.replace(USER_ID, userId)) {
            header("Client-ID", twitchClientId)
        }

        return response.data.isNotEmpty()
    }

    private val client
        get() = HttpClient(Apache) {
            expectSuccess = false
            install(JsonFeature) {
                serializer = KotlinxSerializer(Json.nonstrict)
            }
        }
}