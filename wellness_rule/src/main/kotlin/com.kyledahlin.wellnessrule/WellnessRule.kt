package com.kyledahlin.wellnessrule

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import com.kyledahlin.rulebot.Analytics
import com.kyledahlin.rulebot.ChatInputInteractionContext
import com.kyledahlin.rulebot.GuildCreateContext
import com.kyledahlin.rulebot.bot.Logger.logDebug
import com.kyledahlin.rulebot.bot.Logger.logError
import com.kyledahlin.rulebot.bot.Logger.logInfo
import com.kyledahlin.rulebot.bot.Rule
import com.kyledahlin.wellness.models.WellnessResponse
import discord4j.core.`object`.entity.channel.MessageChannel
import discord4j.core.event.domain.VoiceStateUpdateEvent
import discord4j.discordjson.json.ApplicationCommandRequest
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import suspendCreateMessage
import javax.inject.Inject

private val ONE_DAY = 1000 * 60 * 60 * 24
//private val SIX_HOURS = 5 * 1000

class WellnessRule @Inject constructor(
    private val _analytics: Analytics,
    private val _wellnessStorage: WellnessStorage
) : Rule("Wellness") {

    private val cache = mutableMapOf<String, Long>()

    override fun handlesCommand(name: String): Boolean {
        return name == "wellness-register" || name == "wellness-deregister"
    }

    override suspend fun onGuildCreate(context: GuildCreateContext) {
        super.onGuildCreate(context)
        val registerRequest = ApplicationCommandRequest.builder()
            .name("wellness-register")
            .description("Register this channel for wellness alerts")
            .build()

        context.registerApplicationCommand(registerRequest)

        val deregisterRequest = ApplicationCommandRequest.builder()
            .name("wellness-deregister")
            .description("Deregister this channel for wellness alerts")
            .build()

        // context.registerApplicationCommand(deregisterRequest)
    }

    override suspend fun onSlashCommand(context: ChatInputInteractionContext) {

        logInfo { "handling slash for wellness, command: [${context.name}]" }
        when (context.name) {
            "wellness-register" -> handleRegister(context)
            "wellness-deregister" -> handleDeregister(context)
            else -> logError { "wellness tried to handle command that didnt match" }
        }
    }

    private suspend fun handleDeregister(context: ChatInputInteractionContext) {
        _wellnessStorage.deleteChannelIdForGuild(context.guildId)
        context.reply {
            withEphemeral()
            content { "Successfully deregistered all channels for wellness alerts" }
        }
    }

    private suspend fun handleRegister(context: ChatInputInteractionContext) {
        _wellnessStorage.saveChannelIdForGuild(context.channelId, context.guildId)
        context.reply {
            withEphemeral()
            content { "Successfully registered this channel for wellness alerts" }
        }
    }

    //TODO: refactor this out of using the discord4j methods
    override suspend fun onVoiceUpdate(event: VoiceStateUpdateEvent) {
        logDebug { "wellness got voice ${event.current}" }
        val userCouldBeNotified = cache[event.current.userId.asString()]?.let {
            val difference = System.currentTimeMillis() - it
            difference > ONE_DAY
        } ?: true
        logDebug { "checking for message, user could be notified: [$userCouldBeNotified]" }
        if (event.isJoinEvent && userCouldBeNotified) {
            cache[event.current.userId.asString()] = System.currentTimeMillis()
            val channelSnowflake = _wellnessStorage.getChannelIdForGuild(event.current.guildId) ?: return
            (event.client.getChannelById(channelSnowflake).block() as MessageChannel)
                .suspendCreateMessage {
                    content {
                        "Looks like you intend to game <@${event.current.userId.asString()}>! Before you begin please make sure your posture " +
                                "is good and you have a water source on your desk : )"
                    }
                }
        }
    }

    override suspend fun configure(data: Any): Either<Any, Any> {
        return Either.catch({
            logError { "could not deserialize to configure wellness, $it" }
            "failed to deserialize"
        }, {
            Json.decodeFromString<JsonObject>(data as String)
        }).flatMap { json ->
            val toDisable = (json["toDisable"] as? JsonArray)?.map { it.jsonPrimitive.content } ?: emptyList()
            val toEnable = (json["toEnable"] as? JsonArray)?.map { it.jsonPrimitive.content } ?: emptyList()
            if (toDisable.isNotEmpty() || toEnable.isNotEmpty()) {
                _wellnessStorage.disableForGuild(toDisable)
                _wellnessStorage.enableForGuild(toEnable)
                WellnessResponse("successfully enabled for ${toEnable.size} and disabled for ${toDisable.size} guilds").right()
            } else {
                WellnessResponse("configure was called but no toDisable was given").left()
            }
        }
    }
}