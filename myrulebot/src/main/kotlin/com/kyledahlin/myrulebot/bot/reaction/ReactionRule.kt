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
import com.kyledahlin.myrulebot.bot.sf
import com.kyledahlin.rulebot.DiscordCache
import com.kyledahlin.rulebot.bot.*
import discord4j.core.`object`.reaction.ReactionEmoji
import discord4j.core.`object`.util.Snowflake
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.json.JsonObject
import javax.inject.Inject

/**
 * Automatically add reactions to users messages, configurable through the rest api
 */
@MyRuleBotScope
class ReactionRule @Inject constructor(
    localStorage: LocalStorage,
    val getDiscordWrapperForEvent: GetDiscordWrapperForEvent,
    val cache: DiscordCache,
    val reactionStorage: ReactionStorage
) :
    Rule("Reactions", localStorage, getDiscordWrapperForEvent) {

    override suspend fun handleEvent(event: RuleBotEvent): Boolean {
        val wrapper = getDiscordWrapperForEvent(event)
        val guildId = wrapper?.getGuildId()
        if (event !is MessageCreated || guildId == null) return false

        val guildWrapper = cache.getGuildWrapper(guildId) ?: return false

        val reactions = reactionStorage.getReactionsForMember(guildId, event.author)
        reactions
            .mapNotNull { guildWrapper.getGuildEmojiForId(it) }
            .forEach { wrapper.addEmoji(ReactionEmoji.custom(it)) }

        return false
    }

    override suspend fun configure(data: Any): Any {
        logDebug("configure reaction: $data")
        val command = Json(JsonConfiguration.Stable.copy(isLenient = true)).parse(Command.serializer(), data.toString())
        return when (command.action) {
            "add"    -> addReaction(command)
            "list"   -> getEmojiData(command.guildId.sf())
            "remove" -> removeReaction(command)
            else     -> JsonObject(emptyMap())
        }
    }

    private suspend fun addReaction(command: Command): Any {
        val (guildId, _, member, emoji) = command
        try {
            reactionStorage.storeReactionForMember(member!!.sf(), guildId.sf(), emoji!!.sf())
        } catch (e: Exception) {
            logError("unable to perform add command: ${e.message}")
        }

        return JsonObject(emptyMap())
    }

    private suspend fun removeReaction(command: Command): Any {
        val (guildId, _, member, emoji) = command
        try {
            reactionStorage.removeReactionForMember(member!!.sf(), guildId.sf(), emoji!!.sf())
        } catch (e: Exception) {
            logError("unable to perform remove command: ${e.message}")
        }
        return JsonObject(emptyMap())
    }

    private suspend fun getEmojiData(guildId: Snowflake): GuildInfo {
        logDebug("sending emoji data")
        val guildWrapper = cache.getGuildWrapper(guildId)
        val guildEmojis = try {
            guildWrapper
                ?.getEmojiNameSnowflakes()
                ?.map { Emoji(it.first, it.second.asString()) }
                ?: emptyList()
        } catch (e: java.lang.Exception) {
            logError("could not get emoji list")
            emptyList<Emoji>()
        }

        val members = try {
            guildWrapper
                ?.getMemberNameSnowflakes()
                ?.map { Member(it.first, it.second.asString()) }
                ?: emptyList()
        } catch (e: java.lang.Exception) {
            logError("could not get member list")
            emptyList<Member>()
        }

        return GuildInfo(members, guildEmojis)
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

@Serializable
data class GuildInfo(
    val members: List<Member>,
    val emojis: List<Emoji>
)

@Serializable
data class Member(
    val name: String,
    val snowflake: String
)

@Serializable
data class Emoji(
    val name: String,
    val snowflake: String
)