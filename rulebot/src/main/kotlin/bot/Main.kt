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
import bot.soundboard.SoundboardRule
import discord4j.core.DiscordClientBuilder
import discord4j.core.`object`.entity.MessageChannel
import discord4j.core.`object`.util.Snowflake
import discord4j.core.event.domain.lifecycle.ReadyEvent
import discord4j.core.event.domain.message.MessageCreateEvent
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.runBlocking
import suspendCreateMessage

private val mRules = mutableSetOf<Rule>()

private const val RULES = "rules please"
private const val LOG_LEVEL = "--log-level"
private const val IS_BETA = "--beta"

fun main(args: Array<String>) {
    val mIds = mutableSetOf<Snowflake>()
    val storage = LocalStorageImpl()

    val metaArgs = parseArgs(args)
    Logger.setLogLevel(metaArgs[LOG_LEVEL] as? LogLevel ?: LogLevel.DEBUG)
    val isBeta = metaArgs[IS_BETA] as? Boolean
    Logger.logDebug("is Beta? $isBeta")
    val tokenFile = if (isBeta == true) "betatoken.txt" else "token.txt"
    val client = DiscordClientBuilder(getTokenFromFile(tokenFile)).build()

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
                    ScoreboardRule(storage),
                    RockPaperScissorsRule(mIds, storage),
                    SoundboardRule(storage)
                )
            )
        }

    client.eventDispatcher.on(MessageCreateEvent::class.java)
        .filter { it.message.author.map { user -> !user.isBot }.orElse(false) }
        .filter { event -> event.message.content.isPresent }
        .subscribe({ messageEvent ->
            runBlocking {
                Logger.logDebug("got message event $messageEvent")
                mRules.any {
                    Logger.logDebug("handling messaging for ${it.ruleName}")
                    val wasHandled = it.handleRule(messageEvent)
                    Logger.logDebug("message was ${if (wasHandled) "" else "not "}handled by ${it.ruleName}")
                    wasHandled
                }
                if (messageEvent.message.content.get() == RULES) {
                    messageEvent.message.channel.awaitFirstOrNull()?.printRules()
                }
            }
        }, { throwable ->
            println("throwable in subscription, $throwable")
        })

    client.login().block()
}

//TODO: need a cleaner way to get the command line args
private fun parseArgs(args: Array<String>): Map<String, Any> {
    val result = mutableMapOf<String, Any>()
    val logIndex = args.indexOf("--log-level")
    if (logIndex != -1) {
        val logLevel = args[logIndex + 1].toUpperCase()
        result[LOG_LEVEL] = LogLevel.valueOf(logLevel)
    }

    val betaIndex = args.indexOf("--beta")
    if (betaIndex != -1) {
        result[IS_BETA] = true
    }
    return result
}

private suspend fun MessageChannel.printRules() {
    val ruleMessages = mRules
        .filterNot { it.getExplanation() == null }
        .joinToString(separator = "\n") { "${it.ruleName}:\t${it.getExplanation()}" }
    suspendCreateMessage(ruleMessages)
}