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
import com.kyledahlin.rulebot.Analytics
import com.kyledahlin.rulebot.RuleBot
import com.kyledahlin.rulebot.bot.DaggerBotComponent
import com.kyledahlin.rulebot.bot.LogLevel
import discord4j.common.util.Snowflake
import java.io.IOException

object MyRulebot {
    fun create(
        token: String,
        logLevel: LogLevel,
        analytics: Analytics,
        rulesToLog: Collection<String> = emptySet()
    ): RuleBot {
        val myRules = DaggerMyRuleBotComponent
            .builder()
            .build()
            .rules()
        return RuleBot.make(token = token, logLevel = logLevel, analytics = analytics, rules = myRules, rulesToLog = rulesToLog)
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

open class MyRuleBotException(val ruleName: String? = null, override val message: String) : Exception(message)