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

import arrow.core.Either
import arrow.core.right
import com.kyledahlin.myrulebot.bot.MyRuleBotScope
import com.kyledahlin.rulebot.Analytics
import com.kyledahlin.rulebot.DiscordCache
import com.kyledahlin.rulebot.bot.GetDiscordWrapperForEvent
import com.kyledahlin.rulebot.bot.MessageCreated
import com.kyledahlin.rulebot.bot.Rule
import com.kyledahlin.rulebot.bot.RuleBotEvent
import com.kyledahlin.rulebot.sf
import discord4j.common.util.Snowflake
import discord4j.core.`object`.reaction.ReactionEmoji
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject

/**
 * Automatically add reactions to users messages, configurable through the rest api
 */
@MyRuleBotScope
class ReactionRule @Inject constructor(
    val getDiscordWrapperForEvent: GetDiscordWrapperForEvent,
    val cache: DiscordCache,
    val reactionStorage: ReactionStorage,
    private val analytics: Analytics
) :
    Rule("Reactions", getDiscordWrapperForEvent) {

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

    override suspend fun configure(data: Any): Either<Exception, Any> {
        logDebug("configure reaction: $data")
        val command = Json { isLenient = true }.decodeFromString(Command.serializer(), data.toString())
        return when (command.action) {
            "add" -> addReaction(command)
            "list" -> getEmojiData(command.guildId.sf())
            "remove" -> removeReaction(command)
            else -> emptyMap<String, String>()
        }.right()
    }

    private suspend fun addReaction(command: Command) {
        val (guildId, _, member, emoji) = command
        try {
            reactionStorage.storeReactionForMember(member!!.sf(), guildId.sf(), emoji!!.sf())
        } catch (e: Exception) {
            analytics.logRuleFailed(ruleName, "unable to perform add command: ${e.message}")
            logError("unable to perform add command: ${e.message}")
        }
    }

    private suspend fun removeReaction(command: Command) {
        val (guildId, _, member, emoji) = command
        try {
            reactionStorage.removeReactionForMember(member!!.sf(), guildId.sf(), emoji!!.sf())
        } catch (e: Exception) {
            analytics.logRuleFailed(ruleName, "unable to perform remove command: ${e.message}")
            logError("unable to perform remove command: ${e.message}")
        }
    }

    private suspend fun getEmojiData(guildId: Snowflake): GuildInfo {
        val guildWrapper = cache.getGuildWrapper(guildId)
        val guildEmojis = try {
            guildWrapper
                ?.getEmojiNameSnowflakes()
                ?.map { Emoji(it.first, it.second.asString()) }
                ?: emptyList()
        } catch (e: java.lang.Exception) {
            analytics.logRuleFailed(ruleName, "could not load emoji list for guild: ${guildWrapper?.name}")
            logError("could not get emoji list")
            emptyList()
        }

        val addedEmojis = reactionStorage
            .getStoredReactions(guildId)
            .mapNotNull { reaction ->
                val emojiName = guildEmojis.firstOrNull { it.id == reaction.emoji.asString() }?.name
                if (emojiName == null) null else AddedEmoji(
                    emojiName,
                    reaction.member.asString(),
                    reaction.emoji.asString()
                )
            }

        return GuildInfo(guildEmojis, addedEmojis)
    }

    override fun getExplanation(): String {
        return "Automatically add certain reactions to certain members messages, must be configured through the web portal"
    }

    override val priority: Priority
        get() = Priority.LOW

    @Serializable
    data class Command(
        val guildId: String,
        val action: String? = null,
        val member: String? = null,
        val emoji: String? = null
    )

}

@Serializable
@SerialName("guild_info")
data class GuildInfo(
    val emojis: List<Emoji>,
    val addedEmojis: List<AddedEmoji>
)

@Serializable
data class Emoji(
    val name: String,
    val id: String
)

@Serializable
data class AddedEmoji(
    val emojiName: String,
    val memberId: String,
    val emojiId: String
)