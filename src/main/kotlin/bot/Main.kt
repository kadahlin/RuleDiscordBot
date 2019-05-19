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
package bot

import bot.jojorule.JojoMemeRule
import bot.leaguerule.LeagueRule
import bot.scoreboard.ScoreboardRule
import discord4j.core.DiscordClientBuilder
import discord4j.core.`object`.entity.MessageChannel
import discord4j.core.`object`.util.Snowflake
import discord4j.core.event.domain.lifecycle.ReadyEvent
import discord4j.core.event.domain.message.MessageCreateEvent

private val mRules = mutableSetOf<Rule>()

private const val RULES = "rules please"

fun main(args: Array<String>) {
    val mIds = mutableSetOf<Snowflake>()
    val storage = LocalStorageImpl()

    val client = DiscordClientBuilder(getTokenFromFile("betatoken.txt")).build()
    parseAndSetLogLevel(args)
    client.eventDispatcher.on(ReadyEvent::class.java)
        .subscribe { ready ->
            println("RuleBot is logged in as " + ready.self.username)
            mIds.add(ready.self.id)
            mRules.addAll(
                listOf(
                    TimeoutRule(storage),
                    LeagueRule(storage),
                    JojoMemeRule(storage),
                    ConfigureBotRule(mIds, storage),
                    ScoreboardRule(storage)
                )
            )
        }

    client.eventDispatcher.on(MessageCreateEvent::class.java)
        .map { msg -> msg.message }
        .subscribe { msg ->
            val authorId = msg.author.get().id
            if (!mIds.contains(authorId)) {
                val content = if (msg.content.isPresent) {
                    msg.content.get()
                } else {
                    null
                }
                println("content is $content")
                if (content != null) {
                    mRules.any {
                        val handled = it.handleRule(msg).block()!!
                        if (handled) {
                            Logger.logDebug("message was handled by ${it.ruleName}")
                        }
                        handled
                    }
                }
                if (content == RULES) {
                    msg.channel.subscribe { printRules(it) }
                }
            }
        }

    client.login().block()
}

private fun parseAndSetLogLevel(args: Array<String>) {
    val logIndex = args.indexOf("--log-level")
    if (logIndex == -1) {
        Logger.setLogLevel(LogLevel.DEBUG)
    } else {
        val logLevel = args[logIndex + 1].toUpperCase()
        Logger.setLogLevel(LogLevel.valueOf(logLevel))
    }
}

private fun printRules(channel: MessageChannel) {
    val ruleMessages = mRules
        .filterNot { it.getExplanation() == null }
        .joinToString(separator = "\n") { "${it.ruleName}:\t${it.getExplanation()}" }
    channel.createMessage(ruleMessages).subscribe()
}