/*
*Copyright 2019 Kyle Dahlin
*
*Licensed under the Apache License, Version 2.0 (the "License");
*you may not use this file except in compliance with the License.
*You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing, software
*distributed under the License is distributed on an "AS IS" BASIS,
*WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*See the License for the specific language governing permissions and
*limitations under the License.
*/
package bot.twitch

import bot.LocalStorage
import bot.Rule
import discord4j.core.DiscordClient
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.MessageChannel
import discord4j.core.`object`.util.Snowflake
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import reactor.core.publisher.Mono
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.fixedRateTimer

private const val TWITCH_PREFIX = "!twitch"
private const val TIMER_DURATION = 20 * 1000L

private const val ADD_PREFIX = "add "
private const val REMOVE_PREFIX = "remove "

private val ADD_COMMAND = "$ADD_PREFIX[a-zA-Z0-9_]{4,25}".toRegex()
private val REMOVE_COMMAND = "$REMOVE_PREFIX[a-zA-Z0-9_]{4,25}".toRegex()

internal class TwitchRule(private val client: DiscordClient, storage: LocalStorage) : Rule("Twitch", storage) {

    private val twitchBackend: TwitchBackend = TwitchBackendImpl()
    private val currentStreams = ConcurrentHashMap<String, Boolean>()

    init {
        fixedRateTimer(name = "twitchTimer", initialDelay = 0, period = TIMER_DURATION) {
            checkStreams(client)
//            cancel()
        }
    }

    override fun handleRule(message: Message): Mono<Boolean> {
        val content = message.content.get()
        if (!content.startsWith(TWITCH_PREFIX)) {
            return Mono.just(false)
        }

        parseCommand(content, message)
        return Mono.just(true)
    }

    private fun parseCommand(content: String, message: Message) {
        val outputMessage = when {
            content.getAddStreamName() != null -> addStream(content.getAddStreamName()!!, message)
            content.getRemoveStreamName() != null -> removeStream(content.getRemoveStreamName()!!)
            else -> "Missing valid command and/or valid stream name"
        }

        message.channel.flatMap { it.createMessage(outputMessage) }.subscribe()
    }

    private fun addStream(streamName: String, message: Message): String = transaction {

        val userId = runBlocking { twitchBackend.getIdForUsername(streamName) }
            ?: return@transaction "This user does not seem to exist"

        val existing = StreamNames.select {
            StreamNames.userId eq userId
        }.count()

        if (existing != 0) {
            return@transaction "Stream name is already registered"
        }

        StreamNames.insert {
            it[StreamNames.username] = streamName
            it[StreamNames.userId] = userId
            it[StreamNames.channelId] = message.channel.block()!!.id.asString()
        }

        "Listening for streamer $streamName"
    }

    private fun removeStream(streamName: String): String = transaction {
        val userId = runBlocking { twitchBackend.getIdForUsername(streamName) }
            ?: return@transaction "This user does not seem to exist"

        StreamNames.deleteWhere { StreamNames.userId eq userId }
        "Removed streamer"
    }

    override fun getExplanation(): String? {
        return StringBuilder()
            .appendln("Get updates for when certain twitch channels go live")
            .appendln("All commands start with $TWITCH_PREFIX")
            .appendln("\t'$ADD_PREFIX<streamname>' to add a stream to listen to")
            .appendln("\t'$REMOVE_PREFIX<streamname>' to remove an existing stream")
            .toString()
    }

    private fun checkStreams(client: DiscordClient) = transaction {
        println("checking registered streams")
        StreamNames.selectAll().forEach {
            val userId = it[StreamNames.userId]
            val isLive = runBlocking { twitchBackend.isUserLive(userId) }
            logDebug("user with id $userId is live? [$isLive]")
            if ((currentStreams[userId] == null || currentStreams[userId] == false) && isLive) {
                val discordChannelId = it[StreamNames.channelId]
                client.getChannelById(Snowflake.of(discordChannelId))
                    .map { channel -> channel as MessageChannel }
                    .flatMap { messageChannel -> messageChannel?.createMessage("${it[StreamNames.username]} is now live :slight_smile:") }
                    .subscribe()
            }
            currentStreams[userId] = isLive
        }
    }

}

private fun String.getAddStreamName() = ADD_COMMAND.find(this)?.value?.removePrefix(ADD_PREFIX)

private fun String.getRemoveStreamName() = REMOVE_COMMAND.find(this)?.value?.removePrefix(REMOVE_PREFIX)

object StreamNames : Table() {
    val userId = varchar("user_id", 12).primaryKey()
    val username = varchar("username", 64)
    val channelId = varchar("channel_id", 64)
}
