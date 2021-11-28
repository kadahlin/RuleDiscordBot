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
import com.kyledahlin.rulebot.RuleBot.Builder
import com.kyledahlin.rulebot.bot.*
import com.kyledahlin.rulebot.bot.Logger.logDebug
import discord4j.common.util.Snowflake
import discord4j.core.DiscordClient
import discord4j.core.event.domain.VoiceStateUpdateEvent
import discord4j.core.event.domain.guild.GuildCreateEvent
import discord4j.core.event.domain.interaction.ButtonInteractionEvent
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.event.domain.interaction.UserInteractionEvent
import discord4j.core.event.domain.lifecycle.ReadyEvent
import discord4j.core.event.domain.message.MessageCreateEvent
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import javax.inject.Inject

/**
 * Entry point for using bot modules based on [Rule]
 *
 * See the DSL in [Builder] for how to create a rulebot
 */
class RuleBot private constructor(
    private val token: String,
    private val ruleManager: RuleManager,
    private val logLevel: LogLevel,
    private val rulesToLog: Set<String>,
    private val analytics: Analytics
) {

    private lateinit var client: DiscordClient

    /**
     * Builder for making a new [RuleBot]
     */
    @RuleBotScope
    class Builder @Inject internal constructor(
        private val token: String,
        private val ruleManager: RuleManager,
        private val analytics: Analytics
    ) {
        var logLevel: LogLevel = LogLevel.DEBUG
        private var rulesToLog = mutableSetOf<String>()

        fun logRules(vararg rules: String): Builder = apply {
            rules.forEach { rulesToLog.add(it) }
        }

        fun logRule(rule: String): Builder = apply {
            rulesToLog.add(rule)
        }

        fun addRules(rules: Collection<Rule>): Builder = apply {
            ruleManager.addRules(rules)
        }

        fun build(): RuleBot {
            return RuleBot(token, ruleManager, logLevel, rulesToLog, analytics)
        }
    }

    private val handler = CoroutineExceptionHandler { _, exception ->
        Logger.logError { "Exception on event, $exception" }
    }

    fun start() {
        Logger.setRulesToLog(rulesToLog)
        Logger.setLogLevel(logLevel)
        client = DiscordClient.create(token)
        val gateway = client.login().block()!!

        ruleManager.setContext(client)

        gateway.on(ReadyEvent::class.java)
            .subscribe { ready ->
                GlobalScope.launch {
                    analytics.logLifecycle(
                        "logged_in",
                        "RuleBot is logged in as ${ready.self.username} for ${ready.guilds.size} guilds"
                    )
                }
            }

        gateway.on(MessageCreateEvent::class.java)
            .filter { it.message.author.map { user -> !user.isBot }.orElse(false) }
            .filter { event -> event.message.content.isNotEmpty() }
            .subscribe({ messageEvent ->
                runBlocking { ruleManager.processMessageCreateEvent(messageEvent) }
            }, { throwable ->
                Logger.logError { "throwable in message create subscription, $throwable" }
                throwable.printStackTrace()
            })

        gateway.on(GuildCreateEvent::class.java)
            .subscribe({
                runBlocking { ruleManager.processGuildCreateEvent(it) }
            }, { throwable ->
                Logger.logError { "throwable in guild create subscription, $throwable" }
            })

        gateway.on(ChatInputInteractionEvent::class.java).subscribe { command ->
            GlobalScope.launch(handler) { ruleManager.processSlashCommand(command) }
        }

        gateway.on(UserInteractionEvent::class.java).subscribe { command ->
            GlobalScope.launch(handler) { ruleManager.processUserInteraction(command) }
        }

        gateway.on(ButtonInteractionEvent::class.java).subscribe { command ->
            GlobalScope.launch(handler) { ruleManager.processButtonEvent(command) }
        }

        gateway.on(VoiceStateUpdateEvent::class.java).subscribe { event ->
            GlobalScope.launch(handler) { ruleManager.processVoiceUpdateEvent(event) }
        }
    }

    suspend fun configureRule(ruleName: String, data: Any): Either<Exception, Any>? {
        return ruleManager.configureRule(ruleName, data)
    }

    suspend fun getRuleNames(): Set<String> {
        return ruleManager.getRuleNames()
    }

    suspend fun getGuildInfo(): List<GuildNameAndId> {
        logDebug { "Request for guild information" }
        return client
            .guilds
            .map { GuildNameAndId(it.name(), it.id().asString()) }
            .collectList()
            .block()!!
    }

    suspend fun getMemberInfo(guildId: String): Collection<MemberNameAndId>? {
        return null
    }

    companion object {
        fun make(
            token: String,
            logLevel: LogLevel,
            analytics: Analytics,
            rules: Collection<Rule>,
            rulesToLog: Collection<String>
        ): RuleBot {
            val coreComponent = DaggerBotComponent
                .builder()
                .setToken(token)
                .setAnalytics(analytics)
                .build()
            val builder = coreComponent.botBuilder().apply {
                addRules(rules)
                logRules(*rulesToLog.toTypedArray())
                this.logLevel = logLevel
            }
            return builder.build()
        }
    }
}

fun String.sf() = Snowflake.of(this)

@Serializable
data class GuildNameAndId(
    val name: String,
    val id: String
)

@Serializable
data class MemberNameAndId(
    val name: String,
    val id: String
)