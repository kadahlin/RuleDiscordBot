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
import com.kyledahlin.myrulebot.bot.reaction.GuildInfo
import com.kyledahlin.myrulebot.bot.reaction.ReactionRule
import com.kyledahlin.rulebot.Response
import com.kyledahlin.rulebot.analytics.Analytics
import com.kyledahlin.rulebot.analytics.RULE_CONFIGURATION
import com.kyledahlin.rulebot.analytics.createAnalytics
import com.kyledahlin.rulebot.bot.LogLevel
import com.kyledahlin.rulebot.bot.Logger
import com.kyledahlin.rulebot.bot.getStringFromResourceFile
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlin.collections.set

private const val LOG_LEVEL = "--log-level"
private const val IS_BETA = "--beta"
private const val LOG_RULES = "--log-rules"
private const val DATABASE_ARGS = "--database"
private const val LOCAL_ANALYTICS = "--local"

fun main(args: Array<String>) {

    val metaArgs = parseArgs(args)

    val rulesToLog = metaArgs[LOG_RULES] as? List<String>
    val logLevel = metaArgs[LOG_LEVEL] as? LogLevel ?: LogLevel.INFO

    val isBeta = metaArgs[IS_BETA] as? Boolean
    println("is Beta? $isBeta")

    val tokenFile = if (isBeta == true) "betatoken.txt" else "token.txt"
    val token = getStringFromResourceFile(tokenFile)

    val databaseArgs = metaArgs[DATABASE_ARGS]
    checkNotNull(databaseArgs) { "No database values were given" }
    val (connectingString, databaseName) = metaArgs[DATABASE_ARGS] as List<String>

    val analytics =
        if (metaArgs[LOCAL_ANALYTICS] == true) LocalAnalytics() else createAnalytics(connectingString, databaseName)
    val rulebot = MyRulebot.create(token, rulesToLog ?: emptySet(), logLevel, analytics, connectingString, databaseName)
    rulebot.start()

    embeddedServer(Netty, 8080) {

        install(ContentNegotiation) {
            json(json = Serializer.format)
        }

        routing {
            post("/rules/{ruleName}") {
                val ruleName = call.parameters["ruleName"]!!
                val data = call.receive<String>()
                Logger.logDebug("got ruleName [$ruleName] with body: [$data]")
                analytics.logLifecycle(RULE_CONFIGURATION, "configuring $ruleName")
                val result = rulebot.configureRule(ruleName, data) ?: JsonPrimitive("an error has occurred")
                call.respond(result)
            }

            get("/rules") {
                val ruleNames = rulebot.getRuleNames()
                println("for rule names got: $rulebot")
                call.respond(Response(data = ruleNames.toList()))
            }

            route("/guilds") {
                get {
                    call.respond(Response(data = rulebot.getGuildInfo()))
                }

                get("/{guildId}") {
                    val list = rulebot.getMemberInfo(call.parameters["guildId"]!!) ?: emptyList()
                    call.respond(Response(data = list))
                }
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

    val localArgs = args.indexOf(LOCAL_ANALYTICS)
    if (localArgs != -1) {
        result[LOCAL_ANALYTICS] = true
    }

    val logRules = args.indexOf(LOG_RULES)
    if (logRules != -1) {
        val rulesToLog = args[logRules + 1].toUpperCase()
        result[LOG_RULES] = rulesToLog.split(",")
    }

    val analytics = args.indexOf(DATABASE_ARGS)
    if (analytics != -1) {
        val analyticsPieces = args[analytics + 1]
        result[DATABASE_ARGS] = analyticsPieces.split(",")
    }
    return result
}

private class LocalAnalytics : Analytics {
    override suspend fun logLifecycle(name: String, data: String) {
        println("lifecycle [$name] $data")
    }

    override suspend fun logRuleFailed(ruleName: String, reason: String) {
        println("rule failed [$ruleName] $reason")
    }
}

object Serializer {
    val format by lazy {
        Json {
            serializersModule = SerializersModule {
                polymorphic(Any::class) {
                    subclass(GuildInfo::class)
                    subclass(ReactionRule.Command::class)
                }
            }
        }
    }
}