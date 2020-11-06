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
import com.xenomachina.argparser.ArgParser
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

fun main(args: Array<String>) {

    val args = ArgParser(args).parseInto(::AppArgs)

    println("is Beta? ${args.isBeta}")

    val tokenFile = if (args.isBeta) "betatoken.txt" else "token.txt"
    val token = getStringFromResourceFile(tokenFile)

    val analytics =
        if (args.localAnalytics) LocalAnalytics() else createAnalytics(
            args.database.first,
            args.database.second
        )
    val rulebot =
        MyRulebot.create(token, emptySet(), args.logLevel, analytics, args.database.first, args.database.second)
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
                val result = try {
                    analytics.logLifecycle(RULE_CONFIGURATION, "configuring $ruleName")
                    rulebot.configureRule(ruleName, data) ?: Response(Response.Error(reason = "unknown failure"))
                } catch (e: Exception) {
                    Response(Response.Error(reason = "unknown failure"))
                }

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

class AppArgs(parser: ArgParser) {
    val isBeta by parser.flagging(
        "-b", "--beta",
        help = "sign in with a beta token"
    )

    val database by parser.storing(
        "-d", "--database",
        help = "comma separated connection string and database name"
    ) {

        val pieces = this.split(",")
        Pair(pieces[0], pieces[1])
    }

    val logLevel by parser.storing(
        "--log-level",
        help = "how many logs to spit out"
    ) {
        when (this) {
            "DEBUG" -> LogLevel.DEBUG
            "INFO" -> LogLevel.INFO
            else -> LogLevel.ERROR
        }
    }

    val localAnalytics by parser.flagging(
        "--local",
        help = "print analytics locally instead of using the database"
    )
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