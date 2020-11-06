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
package com.kyledahlin.myrulebot.bot

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.module.SimpleModule
import com.kyledahlin.rulebot.RuleBot
import com.kyledahlin.rulebot.analytics.Analytics
import com.kyledahlin.rulebot.bot.DaggerBotComponent
import com.kyledahlin.rulebot.bot.LogLevel
import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.MongoCredential
import discord4j.core.`object`.util.Snowflake
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import org.litote.kmongo.util.KMongoConfiguration
import java.io.IOException

const val MONGO_USERNAME = "MONGO_USERNAME"
const val MONGO_PASSWORD = "MONGO_PASSWORD"

object MyRulebot {
    fun create(
        token: String,
        rulesToLog: Collection<String>,
        logLevel: LogLevel,
        analytics: Analytics,
        connectionString: String,
        databaseName: String
    ): RuleBot {
        KMongoConfiguration.registerBsonModule(SimpleModule().apply {
            addSerializer(Snowflake::class.java, SnowflakeSerializer())
            addDeserializer(Snowflake::class.java, SnowflakeDeserializer())
        })
        val dbBuilder = MongoClientSettings.builder()
            .applyConnectionString(ConnectionString(connectionString))
        val mongoUsername = System.getenv(MONGO_USERNAME)
        if (!mongoUsername.isNullOrEmpty()) {
            dbBuilder.credential(
                MongoCredential.createScramSha1Credential(
                    System.getenv(MONGO_USERNAME),
                    "admin",
                    System.getenv(MONGO_PASSWORD).toCharArray()
                )
            )
        }
        val coreComponent = DaggerBotComponent.builder().setToken(token).setAnalytics(analytics)
            .setDatabase(
                KMongo.createClient(dbBuilder.build()).getDatabase(databaseName).coroutine
            )
            .build()
        val myRules = DaggerMyRuleBotComponent.builder().botComponent(coreComponent).build().rules()
        val builder = coreComponent.botBuilder().apply {
            addRules(myRules)
            logRules(*rulesToLog.toTypedArray())
            this.logLevel = logLevel
        }
        return builder.build()
    }
}

class SnowflakeSerializer : JsonSerializer<Snowflake>() {
    @Throws(IOException::class, JsonProcessingException::class)
    override fun serialize(
        value: Snowflake, jgen: JsonGenerator, provider: SerializerProvider?
    ) {
        jgen.writeStartObject()
        jgen.writeStringField("snowflake", value.asString())
        jgen.writeEndObject()
    }
}

class SnowflakeDeserializer : JsonDeserializer<Snowflake>() {
    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Snowflake {
        val node: JsonNode = p.codec.readTree(p)
        return Snowflake.of(node.get("snowflake").asText())
    }
}