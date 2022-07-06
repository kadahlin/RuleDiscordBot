package com.kyledahlin.mtgrule

import com.kyledahlin.rulebot.ChatInputInteractionContext
import com.kyledahlin.rulebot.GuildCreateContext
import com.kyledahlin.rulebot.bot.Logger.logError
import com.kyledahlin.rulebot.bot.Logger.logInfo
import com.kyledahlin.rulebot.bot.Rule
import com.kyledahlin.skryfall.SkryfallClient
import com.kyledahlin.skryfall.Success
import com.kyledahlin.skryfall.queries.CardText
import discord4j.core.`object`.command.ApplicationCommandOption
import discord4j.discordjson.json.ApplicationCommandOptionData
import discord4j.discordjson.json.ApplicationCommandRequest
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject

private const val MTG_SEARCH_NAME = "mtg-search"

class MtgRule @Inject constructor(
    private val _client: SkryfallClient
) : Rule("MtgRule") {

    override fun handlesCommand(name: String): Boolean {
        return name == MTG_SEARCH_NAME
    }

    override suspend fun onGuildCreate(context: GuildCreateContext) {
        super.onGuildCreate(context)
        val registerRequest = ApplicationCommandRequest.builder()
            .name(MTG_SEARCH_NAME)
            .description("Mtg card lookup")
            .addOption(
                ApplicationCommandOptionData.builder()
                    .name("name")
                    .description("Card name")
                    .type(ApplicationCommandOption.Type.STRING.value)
                    .required(true)
                    .build()
            )
            .build()

        context.registerApplicationCommand(registerRequest)
    }

    override suspend fun onSlashCommand(context: ChatInputInteractionContext) {
        logInfo { "handling slash for mtg search, command: [${context.name}]" }
        when (context.name) {
            MTG_SEARCH_NAME -> handleSearch(context)
            else -> logError { "mtg search tried to handle command that didnt match" }
        }
    }

    private suspend fun handleSearch(context: ChatInputInteractionContext) {
        val cardName = context.getOption("name")
        if (cardName == null) {
            context.reply {
                withEphemeral()
                content { "Missing card option" }
            }
            return
        }

        context.deferReply(withEphemeral = false)
        val response = try {
            _client.searchCards(CardText.name(cardName))
        } catch (e: Exception) {
            context.editReply {
                content { "No cards found for $cardName" }
            }
            return
        }
        if (response is Success && response.data.isNotEmpty()) {
            context.editReply {
                addEmbed {
                    image { response.data.first().imageUris?.get("normal")!!.jsonPrimitive.content }
                }
            }
        } else {
            context.editReply {
                content { "No cards found for $cardName" }
            }
        }
    }
}