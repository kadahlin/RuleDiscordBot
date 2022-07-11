package com.kyledahlin.destiny

import com.kyledahlin.rulebot.ChatInputInteractionContext
import com.kyledahlin.rulebot.GuildCreateContext
import com.kyledahlin.rulebot.bot.Logger.logError
import com.kyledahlin.rulebot.bot.Logger.logInfo
import com.kyledahlin.rulebot.bot.Rule
import discord4j.discordjson.json.ApplicationCommandRequest
import javax.inject.Inject

private const val TODAYS_MODS_NAME = "todays-mods"

class Destiny2Rule @Inject constructor(private val _api: Destiny2Api) : Rule("Destiny2") {

    override fun handlesCommand(name: String): Boolean {
        return name == TODAYS_MODS_NAME
    }

    override suspend fun onGuildCreate(context: GuildCreateContext) {
        super.onGuildCreate(context)

        context.registerApplicationCommand(
            ApplicationCommandRequest.builder()
                .name(TODAYS_MODS_NAME)
                .description("Get information on the mods being sold today")
                .build()
        )
    }

    override suspend fun onSlashCommand(context: ChatInputInteractionContext) {
        logInfo { "Handling slash for destiny 2, ${context.name}" }
        when (context.name) {
            TODAYS_MODS_NAME -> handleTodaysMods(context)
            else -> logError { "Not a valid command for this rule" }
        }
    }

    private suspend fun handleTodaysMods(context: ChatInputInteractionContext) {
        context.deferReply(withEphemeral = false)
        _api.getTodaysModInformation()
            .fold({ failureReason ->
                context.editReply {
                    withEphemeral()
                    content { "Couldnt get mods: $failureReason from server" }
                }
            }, { pairs ->
                context.editReply {
                    content { pairs.joinToString { it.first } }
                    pairs.forEach { pair ->
                        addEmbed { image { pair.second } }
                    }
                }
            })
    }
}