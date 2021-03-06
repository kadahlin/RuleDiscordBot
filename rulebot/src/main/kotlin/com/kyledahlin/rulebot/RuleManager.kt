/*
*Copyright 2020 Kyle Dahlin
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

import com.kyledahlin.rulebot.bot.*
import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.entity.MessageChannel
import discord4j.core.event.domain.message.MessageCreateEvent
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.awaitFirstOrNull
import suspendChannel
import suspendCreateMessage
import suspendGuild
import javax.inject.Inject

private const val RULES = "rules please"

@RuleBotScope
internal class RuleManager @Inject constructor(
    private val cache: DiscordCache
) {

    private var _rules = mutableSetOf<Rule>()

    fun addRules(rules: Collection<Rule>) {
        _rules.apply {
            addAll(rules)
            sortedBy { it.priority.ordinal }
        }
    }

    suspend fun configureRule(ruleName: String, data: Any): Any? {
        return _rules.firstOrNull { it.ruleName.equals(ruleName, ignoreCase = true) }?.configure(data)
    }

    fun processMessageCreateEvent(messageEvent: MessageCreateEvent) {
        Logger.logDebug("processing event for ${_rules.size} rules")
        GlobalScope.launch {
            Logger.logDebug("got message event $messageEvent")
            val (messageCreated, guild, channel, member) = convertMessageCreateEvent(messageEvent)
            cache.createEventWrapperEntry(
                messageCreated as RuleBotEvent,
                channel as MessageChannel,
                guild as Guild,
                member as Member
            )
            _rules.any {
                Logger.logDebug("handling messaging for ${it.ruleName}")
                val wasHandled = it.handleEvent(messageCreated)
                Logger.logDebug("message was ${if (wasHandled) "" else "not "}handled by ${it.ruleName}")
                wasHandled
            }
            val content = messageEvent.message.content.get()
            if (content.startsWith(RULES)) {
                val contentPieces = messageEvent.message.content.get().split(" ")
                if (contentPieces.size > 2) {
                    messageEvent.message.channel.awaitFirstOrNull()?.printExplanationOfRule(contentPieces[2])
                } else {
                    messageEvent.message.channel.awaitFirstOrNull()?.printRules()
                }
            }
        }
    }

    private suspend fun convertMessageCreateEvent(event: MessageCreateEvent): List<Any?> {
        val content = event.message.content.get()
        val guild = event.suspendGuild()
        val author = event.message.author.get().id
        val snowflakes = event.message.getSnowflakes()
        val channel = event.message.suspendChannel()
        val member = if (event.member.isPresent) event.member.get() else null
        val attachments = event.message.attachments.map { AttachmentUrl(it.url) }

        val messageCreated = MessageCreated(event.message.id, content, author, snowflakes, attachments)
        return listOf(messageCreated, guild, channel, member)
    }

    private suspend fun MessageChannel.printRules() {
        val ruleMessages = _rules
            .filterNot { it.getExplanation() == null }
            .sortedBy { it.ruleName }
            .mapIndexed { index, rule -> "${index}. ${rule.ruleName}" }
            .joinToString(separator = "\n")

        suspendCreateMessage(ruleMessages)
    }

    private suspend fun MessageChannel.printExplanationOfRule(name: String) {
        val ruleMessage = _rules.firstOrNull { it.ruleName == name }?.getExplanation()
        if (ruleMessage == null) {
            suspendCreateMessage("No rule with that name")
        } else {
            suspendCreateMessage(ruleMessage)
        }
    }

    internal suspend fun getRuleNames(): Set<String> {
        return _rules.map { it.ruleName }.toSet()
    }
}

