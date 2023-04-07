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

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.kyledahlin.models.basicResponseModule
import com.kyledahlin.myrulebot.bot.MyRulebot
import com.kyledahlin.rulebot.Analytics
import com.kyledahlin.models.Response
import com.kyledahlin.rulebot.RuleBot
import com.kyledahlin.rulebot.bot.LogLevel
import com.kyledahlin.rulebot.bot.Logger.logDebug
import com.kyledahlin.wellness.models.wellnessSerializerModule
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.plus
import org.slf4j.event.Level
import javax.inject.Inject

fun main(args: Array<String>) {
    val logLevel = when (System.getenv("HONKBOT_LOG")) {
        "DEBUG" -> LogLevel.DEBUG
        "INFO" -> LogLevel.INFO
        else -> LogLevel.DEBUG
    }

    val port = System.getenv("PORT").toInt()
    val host = System.getenv("RULEBOT_HOST") ?: "127.0.0.1"
    logDebug { "running on $port" }

    val token = System.getenv("HONKBOT_TOKEN")

    val loader = Thread.currentThread().contextClassLoader
    val stream = loader.getResourceAsStream("serviceAccount.json")
    val options: FirebaseOptions = FirebaseOptions.builder()
        .setCredentials(GoogleCredentials.fromStream(stream))
        .build()

    FirebaseApp.initializeApp(options)

    val analytics = LocalAnalytics()
    val rulebot = MyRulebot.create(
        token = token,
        rulesToLog = emptySet(),
        logLevel = logLevel,
        analytics = analytics
    )
    rulebot.start()

    embeddedServer(Netty, environment = applicationEngineEnvironment {
        applySelfCert(host, port) { mainModule(analytics, rulebot) }
    }).start(wait = true)
}

fun Application.mainModule(analytics: Analytics, ruleBot: RuleBot, json: Json = myRulebotJson) {
    install(ContentNegotiation) {
        json(json)
    }

    install(CallLogging) {
        level = Level.ERROR
    }

    routing {
        rules(analytics, ruleBot)
        guilds(ruleBot)
    }
}

internal class LocalAnalytics @Inject constructor() : Analytics {
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

private val myRulebotJson = Json {
    serializersModule = basicResponseModule + wellnessSerializerModule
}