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

import arrow.core.Either
import com.kyledahlin.rulebot.bot.*
import com.kyledahlin.rulebot.bot.Logger.logDebug
import discord4j.core.DiscordClient
import discord4j.core.event.domain.guild.GuildCreateEvent
import discord4j.core.event.domain.interaction.ButtonInteractionEvent
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.event.domain.interaction.UserInteractionEvent
import discord4j.core.event.domain.message.MessageCreateEvent
import suspendChannel
import suspendGuild
import javax.inject.Inject

@RuleBotScope
internal class RuleManager @Inject constructor() {

    private var _rules = mutableSetOf<Rule>()
    private lateinit var client: DiscordClient

    fun addRules(rules: Collection<Rule>) {
        _rules.apply {
            addAll(rules)
            sortedBy { it.priority.ordinal }
        }
    }

    fun setContext(client: DiscordClient) {
        this.client = client
        _rules.forEach { it.setContext(client) }
    }

    suspend fun configureRule(ruleName: String, data: Any): Either<Exception, Any>? {
        return _rules.firstOrNull { it.ruleName.equals(ruleName, ignoreCase = true) }?.configure(data)
    }

    suspend fun processMessageCreateEvent(messageEvent: MessageCreateEvent) {
        logDebug { "processing event for ${_rules.size} rules" }
        logDebug { "got message event $messageEvent" }
//        val (messageCreated, guild, channel, member) = convertMessageCreateEvent(messageEvent)
//        _rules.any {
//            logDebug { "handling messaging for ${it.ruleName}" }
//            val wasHandled = it.handleEvent(messageCreated)
//            logDebug { "message was ${if (wasHandled) "" else "not "}handled by ${it.ruleName}" }
//            wasHandled
//        }
    }

    suspend fun processGuildCreateEvent(event: GuildCreateEvent) {
        Logger.logInfo { "processing join for guild ${event.guild.name}" }
        _rules.forEach { it.onGuildCreate(GuildCreateContextImpl(client, event)) }
    }

    suspend fun processSlashCommand(event: ChatInputInteractionEvent) {
        logDebug { "checking for chat event ${event.commandName}" }
        val rule = _rules
            .firstOrNull { it.handlesCommand(event.commandName) }
        if (rule == null) {
            logDebug { "got slash command for ${event.commandName} but no matching rule was found" }
        }
        rule?.onSlashCommand(ChatInputInteractionContextImpl(event))
    }

    suspend fun processUserInteraction(event: UserInteractionEvent) {
        logDebug { "checking for user event ${event.commandName}" }
        val rule = _rules
            .firstOrNull { it.handlesCommand(event.commandName) }
        if (rule == null) {
            logDebug { "got user event for ${event.commandName} but no matching rule was found" }
        }
        rule?.onUserCommand(UserInteractionContextImpl(event))
    }

    suspend fun processButtonEvent(event: ButtonInteractionEvent) {
        logDebug { "checking for button event ${event.customId}" }
        _rules.forEach { it.onButtonEvent(ButtonInteractionEventContextImpl(event)) }
    }

    private suspend fun convertMessageCreateEvent(event: MessageCreateEvent): List<Any?> {
        val content = event.message.content
        val guild = event.suspendGuild()
        val author = event.message.author.get().id
        val snowflakes = event.message.getSnowflakes()
        val channel = event.message.suspendChannel()
        val member = if (event.member.isPresent) event.member.get() else null
//        val attachments = event.message.attachments.map { AttachmentUrl(it.url) }

        val messageCreated = MessageCreated(event.message.id, content, author, snowflakes)
        return listOf(messageCreated, guild, channel, member)
    }

    internal suspend fun getRuleNames(): Set<String> {
        return _rules.map { it.ruleName }.toSet()
    }

}

