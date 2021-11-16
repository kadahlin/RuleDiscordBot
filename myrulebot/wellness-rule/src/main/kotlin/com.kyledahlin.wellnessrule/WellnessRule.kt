package com.kyledahlin.wellnessrule

import com.kyledahlin.rulebot.Analytics
import com.kyledahlin.rulebot.ChatInputInteractionContext
import com.kyledahlin.rulebot.GuildCreateContext
import com.kyledahlin.rulebot.bot.Logger.logDebug
import com.kyledahlin.rulebot.bot.Logger.logError
import com.kyledahlin.rulebot.bot.Logger.logInfo
import com.kyledahlin.rulebot.bot.Rule
import discord4j.core.`object`.entity.channel.MessageChannel
import discord4j.core.event.domain.VoiceStateUpdateEvent
import discord4j.discordjson.json.ApplicationCommandRequest
import suspendCreateMessage
import javax.inject.Inject

private val SIX_HOURS = 1000 * 60 * 60 * 6
// private val SIX_HOURS = 10 * 1000

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
            difference > SIX_HOURS
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
}