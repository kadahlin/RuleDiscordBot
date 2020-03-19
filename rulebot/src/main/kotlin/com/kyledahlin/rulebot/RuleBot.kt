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
import com.kyledahlin.rulebot.bot.LogLevel
import com.kyledahlin.rulebot.bot.Logger
import com.kyledahlin.rulebot.bot.Rule
import com.kyledahlin.rulebot.bot.RuleBotScope
import discord4j.core.DiscordClientBuilder
import discord4j.core.event.domain.lifecycle.ReadyEvent
import discord4j.core.event.domain.message.MessageCreateEvent
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
    private val rulesToLog: Set<String>
) {

    /**
     * Builder for making a new [RuleBot]
     */
    @RuleBotScope
    class Builder @Inject internal constructor(private val token: String, private val ruleManager: RuleManager) {
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
            return RuleBot(token, ruleManager, logLevel, rulesToLog)
        }
    }

    fun start() {
        Logger.setRulesToLog(rulesToLog)
        Logger.setLogLevel(logLevel)
        val client = DiscordClientBuilder(token).build()

        client.eventDispatcher.on(ReadyEvent::class.java)
            .subscribe { ready ->
                println("RuleBot is logged in as ${ready.self.username} for ${ready.guilds.size} guilds")
                ruleManager.addBotIds(setOf(ready.self.id))

                //add all the logged in guilds for later use
                ready.guilds.map { it.id }.forEach { guildSnowflake ->
                    client.getGuildById(guildSnowflake).subscribe { ruleManager.addGuilds(arrayListOf(it)) }
                }
            }

        client.eventDispatcher.on(MessageCreateEvent::class.java)
            .filter { it.message.author.map { user -> !user.isBot }.orElse(false) }
            .filter { event -> event.message.content.isPresent }
            .subscribe({ messageEvent ->
                ruleManager.processMessageCreateEvent(messageEvent)
            }, { throwable ->
                Logger.logError("throwable in subscription, $throwable")
                throwable.printStackTrace()
            })

        client.login().subscribe()
    }

    suspend fun configureRule(ruleName: String, data: Any): Any? {
        return ruleManager.configureRule(ruleName, data)
    }

    suspend fun getRuleNames(): Set<String> {
        return ruleManager.getRuleNames()
    }
}