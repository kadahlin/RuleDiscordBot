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
package com.kyledahlin.myrulebot.bot.reaction

import com.kyledahlin.myrulebot.bot.MyRuleBotScope
import com.kyledahlin.rulebot.DiscordCache
import com.kyledahlin.rulebot.bot.*
import discord4j.core.`object`.util.Snowflake
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import javax.inject.Inject

@MyRuleBotScope
class ReactionRule @Inject constructor(
    localStorage: LocalStorage,
    val getDiscordWrapperForEvent: GetDiscordWrapperForEvent,
    val cache: DiscordCache,
    reactionStorage: ReactionStorage
) :
    Rule("Reactions", localStorage, getDiscordWrapperForEvent) {

    override suspend fun handleEvent(event: RuleBotEvent): Boolean {
        if (event !is MessageCreated) return false

        return if (event.content.contains("reactions")) {
            val wrapper = getDiscordWrapperForEvent(event)
            wrapper?.getGuildId()?.let { guildId ->
                val guildWrapper = cache.getGuildWrapper(guildId)
                logDebug("gWrapper: $guildWrapper")
                val guildEmojis = guildWrapper?.getEmojiNameSnowflakes() ?: listOf("failed")
                logDebug("emojis: $guildEmojis")
                wrapper.sendMessage(guildEmojis.joinToString(separator = ",") + " end")
            }
            true
        } else {
            false
        }
    }

    override suspend fun configure(data: Any): Any {
        logDebug("configure reaction: $data")
        val command = Json(JsonConfiguration.Stable.copy(isLenient = true)).parse(Command.serializer(), data.toString())
        if(command.action == null) {
            logDebug("sending emoji data")
        }
        val guildWrapper = cache.getGuildWrapper(Snowflake.of(command.guildId))
        val guildEmojis = guildWrapper
            ?.getEmojiNameSnowflakes()
            ?.map { it.first to JsonPrimitive(it.second.asString()) }
            ?: emptyList()

        return JsonObject(mapOf(*guildEmojis.toTypedArray()))
    }

    override fun getExplanation(): String? {
        return "Automatically add certain reactions to certain members messages, must be configured through the web portal"
    }

    override val priority: Priority
        get() = Priority.LOW

}

@Serializable
data class Command(
    val guildId: String,
    val action: String? = null,
    val member: String? = null,
    val emoji: String? = null
)