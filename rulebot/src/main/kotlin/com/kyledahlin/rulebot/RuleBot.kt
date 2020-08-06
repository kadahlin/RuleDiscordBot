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

import com.kyledahlin.rulebot.RuleBot.Builder
import com.kyledahlin.rulebot.analytics.Analytics
import com.kyledahlin.rulebot.bot.LogLevel
import com.kyledahlin.rulebot.bot.Logger
import com.kyledahlin.rulebot.bot.Rule
import com.kyledahlin.rulebot.bot.RuleBotScope
import discord4j.core.DiscordClientBuilder
import discord4j.core.`object`.util.Snowflake
import discord4j.core.event.domain.guild.GuildCreateEvent
import discord4j.core.event.domain.lifecycle.ReadyEvent
import discord4j.core.event.domain.message.MessageCreateEvent
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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
    private val discordCache: DiscordCache,
    private val logLevel: LogLevel,
    private val rulesToLog: Set<String>,
    private val analytics: Analytics
) {

    /**
     * Builder for making a new [RuleBot]
     */
    @RuleBotScope
    class Builder @Inject internal constructor(
        private val token: String,
        private val ruleManager: RuleManager,
        private val cache: DiscordCache,
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
            return RuleBot(token, ruleManager, cache, logLevel, rulesToLog, analytics)
        }
    }

    fun start() {
        Logger.setRulesToLog(rulesToLog)
        Logger.setLogLevel(logLevel)
        val client = DiscordClientBuilder(token).build()

        client.eventDispatcher.on(ReadyEvent::class.java)
            .subscribe { ready ->
                GlobalScope.launch {
                    analytics.logLifecycle(
                        "logged_in",
                        "RuleBot is logged in as ${ready.self.username} for ${ready.guilds.size} guilds"
                    )
                }
                discordCache.addBotIds(setOf(ready.self.id))
            }

        client.eventDispatcher.on(MessageCreateEvent::class.java)
            .filter { it.message.author.map { user -> !user.isBot }.orElse(false) }
            .filter { event -> event.message.content.isPresent }
            .subscribe({ messageEvent ->
                ruleManager.processMessageCreateEvent(messageEvent)
            }, { throwable ->
                Logger.logError("throwable in message create subscription, $throwable")
                throwable.printStackTrace()
            })

        client.eventDispatcher.on(GuildCreateEvent::class.java)
            .subscribe({
                discordCache.addGuild(it.guild)
            }, { throwable ->
                Logger.logError("throwable in guild create subscription, $throwable")
            })

        client.login().subscribe()
    }

    suspend fun configureRule(ruleName: String, data: Any): Any? {
        return ruleManager.configureRule(ruleName, data)
    }

    suspend fun getRuleNames(): Set<String> {
        return ruleManager.getRuleNames()
    }

    suspend fun getGuildInfo(): Collection<GuildNameAndId> {
        return discordCache.getGuildWrappers().map { GuildNameAndId(it.name, it.id.asString()) }
    }

    suspend fun getMemberInfo(guildId: String): Collection<MemberNameAndId>? {
        val wrapper = discordCache.getGuildWrapper(guildId.sf())
        return wrapper
            ?.getMemberNameSnowflakes()
            ?.map { MemberNameAndId(it.first, it.second.asString()) }
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