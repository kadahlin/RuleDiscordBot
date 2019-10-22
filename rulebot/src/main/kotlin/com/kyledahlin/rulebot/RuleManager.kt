package com.kyledahlin.rulebot

import com.kyledahlin.rulebot.bot.Logger
import com.kyledahlin.rulebot.bot.Rule
import discord4j.core.`object`.entity.MessageChannel
import discord4j.core.event.domain.message.MessageCreateEvent
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.runBlocking
import suspendCreateMessage
import javax.inject.Inject

private const val RULES = "rules please"

internal class RuleManager @Inject constructor(rules: @JvmSuppressWildcards Set<Rule>) {

    private val rules: List<Rule> = rules.sortedBy { it.priority.ordinal }

    fun processMessageCreateEvent(messageEvent: MessageCreateEvent) {
        runBlocking {
            Logger.logDebug("got message event $messageEvent")
            rules.any {
                Logger.logDebug("handling messaging for ${it.ruleName}")
                val wasHandled = it.handleRule(messageEvent)
                Logger.logDebug("message was ${if (wasHandled) "" else "not "}handled by ${it.ruleName}")
                wasHandled
            }
            if (messageEvent.message.content.get() == RULES) {
                messageEvent.message.channel.awaitFirstOrNull()?.printRules()
            }
        }
    }

    private suspend fun MessageChannel.printRules() {
        val ruleMessages = rules
            .filterNot { it.getExplanation() == null }
            .joinToString(separator = "\n") { "${it.ruleName}:\t${it.getExplanation()}" }
        suspendCreateMessage(ruleMessages)
    }

}

