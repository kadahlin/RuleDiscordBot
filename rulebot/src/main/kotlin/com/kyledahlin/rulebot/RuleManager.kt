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
                val wasHandled = it.handleEvent(messageEvent)
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

