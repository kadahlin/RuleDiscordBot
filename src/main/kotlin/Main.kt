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
import discord4j.core.DiscordClientBuilder
import discord4j.core.`object`.entity.MessageChannel
import discord4j.core.`object`.util.Snowflake
import discord4j.core.event.domain.lifecycle.ReadyEvent
import discord4j.core.event.domain.message.MessageCreateEvent
import jojorule.JojoMemeRule
import leaguerule.LeagueRule

private lateinit var mId: Snowflake
private lateinit var mRules: List<Rule>

private const val RULES = "rules please"

fun main(args: Array<String>) {
    mRules = listOf(TimeoutRule(), LeagueRule(), BotMentionRule(), JojoMemeRule())

    val client = DiscordClientBuilder(getTokenFromFile("token.txt")).build()
    parseAndSetLogLevel(args)
    client.eventDispatcher.on(ReadyEvent::class.java)
        .subscribe { ready ->
            println("Rule bot is logged in as " + ready.self.username)
            mId = ready.self.id
        }

    client.eventDispatcher.on(MessageCreateEvent::class.java)
        .map { msg -> msg.message }
        .subscribe { msg ->
            val username = msg.author.get().username
            println("message from $username")
            println("content is ${msg.content.get()}")
            if (msg.author.get().username != bot.username) {
                if (msg.content.get() == RULES) {
                    printRules(msg.channel.block()!!)
                } else {
                    mRules.any {
                        it.handleRule(msg).block()!!
                    }
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