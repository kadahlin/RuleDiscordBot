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

import arrow.core.flatMap
import arrow.core.right
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.kyledahlin.myrulebot.bot.MyRulebot
import com.kyledahlin.myrulebot.bot.reaction.GuildInfo
import com.kyledahlin.rulebot.Analytics
import com.kyledahlin.rulebot.GuildNameAndId
import com.kyledahlin.rulebot.MemberNameAndId
import com.kyledahlin.rulebot.Response
import com.kyledahlin.rulebot.bot.LogLevel
import com.kyledahlin.rulebot.bot.Logger
import com.kyledahlin.rulebot.bot.getStringFromPath
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.slf4j.event.Level
import java.io.FileInputStream

fun main() {
    val firebasePath = System.getenv("HONKBOT_CREDENTIALS")
    val tokenPath = System.getenv("HONKBOT_TOKEN")
    val logLevel = when (System.getenv("HONKBOT_LOG")) {
        "DEBUG" -> LogLevel.DEBUG
        "INFO" -> LogLevel.INFO
        else -> LogLevel.ERROR
    }

    val port = System.getenv("PORT").toInt()
    println("running on $port")

    val token = getStringFromPath(tokenPath)
    val serviceAccount = FileInputStream(firebasePath)

    val options: FirebaseOptions = FirebaseOptions.builder()
        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
        .build()

    FirebaseApp.initializeApp(options)

    val analytics =
        LocalAnalytics()
    val rulebot =
        MyRulebot.create(token, emptySet(), logLevel, analytics)
    rulebot.start()

    embeddedServer(Netty, host = System.getenv("RULEBOT_HOST") ?: "127.0.0.1", port = port) {

        install(ContentNegotiation) {
            json(Serializer.format)
        }

        install(CallLogging) {
            level = Level.INFO
        }

        routing {
            post("/rules/{ruleName}") {
                val ruleName = call.parameters["ruleName"]!!
                val data = call.receive<String>()
                Logger.logDebug("got ruleName [$ruleName] with body: [$data]")
                analytics.logLifecycle("Rule config", "call to configure $ruleName")
                val response = rulebot.configureRule(ruleName, data)
                    ?.flatMap { if (it is Unit) EmptyResponse.right() else it.right() }
                    ?.fold({ exception ->
                        Response.error(exception.message ?: "no exception message")
                    }, { value ->
                        Response.success(value)
                    })
                    ?: Response.error("No rule found for this name")
                call.jsonResponse(response)
            }

            get("/rules") {
                val ruleNames = rulebot.getRuleNames()
                println("for rule names got: $ruleNames")
                val data = Response(GetRulesResponse(ruleNames.toList()))
                call.jsonResponse(data)
            }

            route("/guilds") {
                get {
                    call.jsonResponse(Response.success(GetGuildsResponse(rulebot.getGuildInfo())))
                }

                get("/{guildId}") {
                    val list = rulebot.getMemberInfo(call.parameters["guildId"]!!)?.toList() ?: emptyList()
                    call.jsonResponse(Response.success(MemberNameAndIds(list)))
                }
            }
        }
    }.start(wait = true)
}

@Serializable
class GetRulesResponse(val rules: List<String>)

@Serializable
class GetGuildsResponse(val guilds: List<GuildNameAndId>)

@Serializable
class MemberNameAndIds(val members: List<MemberNameAndId>)

private class LocalAnalytics : Analytics {
    override suspend fun logLifecycle(name: String, data: String) {
        println("lifecycle [$name] $data")
    }

    override suspend fun logRuleFailed(ruleName: String, reason: String) {
        println("rule failed [$ruleName] $reason")
    }
}

//workaround until ktor has better generic message wrapping support
suspend inline fun ApplicationCall.jsonResponse(res: Response) {
//    response.pipeline.execute(this, Json.encodeToString(Response.s, res))
    respond(res)
}

val module = SerializersModule {
    polymorphic(Any::class) {
        subclass(GetRulesResponse::class)
        subclass(GetGuildsResponse::class)
        subclass(MemberNameAndIds::class)
        subclass(GuildInfo::class)
        subclass(EmptyResponse::class)
    }
}

@Serializable
object EmptyResponse

object Serializer {
    val format by lazy {
        Json {
            serializersModule = module
        }
    }
}