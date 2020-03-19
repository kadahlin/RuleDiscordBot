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
package com.kyledahlin.myrulebot.app

import com.kyledahlin.myrulebot.bot.MyRulebot
import com.kyledahlin.rulebot.bot.LogLevel
import com.kyledahlin.rulebot.bot.Logger
import com.kyledahlin.rulebot.bot.getStringFromResourceFile
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.serialization.json
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlin.collections.set

private const val LOG_LEVEL = "--log-level"
private const val IS_BETA = "--beta"
private const val LOG_RULES = "--log-rules"

fun main(args: Array<String>) {

    val metaArgs = parseArgs(args)

    val rulesToLog = metaArgs[LOG_RULES] as? List<String>
    val logLevel = metaArgs[LOG_LEVEL] as? LogLevel ?: LogLevel.INFO

    val isBeta = metaArgs[IS_BETA] as? Boolean
    println("is Beta? $isBeta")

    val tokenFile = if (isBeta == true) "betatoken.txt" else "token.txt"
    val token = getStringFromResourceFile(tokenFile)

    val rulebot = MyRulebot.create(token, rulesToLog ?: emptySet(), logLevel)
    GlobalScope.launch {
        rulebot.start()
    }

    embeddedServer(Netty, 8080) {

        install(ContentNegotiation) {
            json()
        }

        routing {
            post("/rules/{ruleName}") {
                val ruleName = call.parameters["ruleName"]!!
                val data = call.receive<String>()
                Logger.logDebug("got ruleName [$ruleName] with body: [$data]")
                val result = rulebot.configureRule(ruleName, data) ?: JsonPrimitive("an error has occurred")
                call.respond(result)
            }

            get("/rules") {
                val ruleNames = rulebot.getRuleNames()
                println("for rule names got: $rulebot")
                call.respond(JsonArray(ruleNames.map { JsonPrimitive(it) }))
            }
        }
    }.start(wait = true)
}

//TODO: need a cleaner way to get the command line args
private fun parseArgs(args: Array<String>): Map<String, Any> {
    val result = mutableMapOf<String, Any>()
    val logIndex = args.indexOf(LOG_LEVEL)
    if (logIndex != -1) {
        val logLevel = args[logIndex + 1].toUpperCase()
        result[LOG_LEVEL] = LogLevel.valueOf(logLevel)
    }

    val betaIndex = args.indexOf(IS_BETA)
    if (betaIndex != -1) {
        result[IS_BETA] = true
    }
    val logRules = args.indexOf(LOG_RULES)
    if (logRules != -1) {
        val rulesToLog = args[logRules + 1].toUpperCase()
        result[LOG_RULES] = rulesToLog.split(",")
    }
    return result
}