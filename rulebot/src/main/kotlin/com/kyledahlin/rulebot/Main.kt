package com.kyledahlin.rulebot

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

import com.kyledahlin.rulebot.bot.*
import discord4j.core.DiscordClientBuilder
import discord4j.core.event.domain.lifecycle.ReadyEvent
import discord4j.core.event.domain.message.MessageCreateEvent

private val mRules = mutableSetOf<Rule>()

private const val LOG_LEVEL = "--log-level"
private const val IS_BETA = "--beta"

fun main(args: Array<String>) {
    var ruleManager: RuleManager? = null

    val metaArgs = parseArgs(args)
    Logger.setLogLevel(metaArgs[LOG_LEVEL] as? LogLevel ?: LogLevel.INFO)
    val isBeta = metaArgs[IS_BETA] as? Boolean
    Logger.logDebug("is Beta? $isBeta")
    val tokenFile = if (isBeta == true) "betatoken.txt" else "token.txt"
    val client = DiscordClientBuilder(getTokenFromFile(tokenFile)).build()

    client.eventDispatcher.on(ReadyEvent::class.java)
        .subscribe { ready ->
            println("RuleBot is logged in as " + ready.self.username)
            ruleManager = DaggerBotComponent.builder().setBotIds(setOf(ready.self.id)).build().ruleManager()
        }

    client.eventDispatcher.on(MessageCreateEvent::class.java)
        .filter { it.message.author.map { user -> !user.isBot }.orElse(false) }
        .filter { event -> event.message.content.isPresent }
        .subscribe({ messageEvent ->
            ruleManager?.processMessageCreateEvent(messageEvent)
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