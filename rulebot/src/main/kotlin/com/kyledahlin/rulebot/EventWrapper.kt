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
package com.kyledahlin.rulebot

import com.kyledahlin.rulebot.bot.RuleBotEvent
import discord4j.core.`object`.entity.*
import discord4j.core.`object`.reaction.ReactionEmoji
import discord4j.core.`object`.util.Snowflake
import discord4j.core.spec.MessageCreateSpec
import discord4j.core.spec.VoiceChannelJoinSpec
import suspendAddReaction
import suspendChannel
import suspendCreateMessage
import suspendDelete
import suspendGetMessageById
import suspendVoiceState

/**
 * Get the meta information about an event. This can be used to do things like add a reaction or send a message.
 */
interface EventWrapper {

    val event: RuleBotEvent

    suspend fun deleteMessage()

    suspend fun sendMessage(message: String)

    suspend fun sendMessage(withSpec: MessageCreateSpec.() -> Unit)

    suspend fun addEmoji(reactionEmoji: ReactionEmoji)

    suspend fun getGuildOwnerId(): Snowflake?

    suspend fun getGuildId(): Snowflake?

    val isDm: Boolean

    fun getRoleIds(): Set<Snowflake>

    suspend fun joinVoiceChannel(with: VoiceChannelJoinSpec.() -> Unit)
}

internal class EventWrapperImpl(
    ruleBotEvent: RuleBotEvent,
    val channel: MessageChannel,
    val guild: Guild?,
    val member: Member
) : EventWrapper {

    override val event = ruleBotEvent

    override suspend fun deleteMessage() {
        channel.suspendGetMessageById(event.id)?.suspendDelete()
    }

    override suspend fun sendMessage(message: String) {
        channel.suspendCreateMessage(message)
    }

    override suspend fun sendMessage(withSpec: MessageCreateSpec.() -> Unit) {
        channel.suspendCreateMessage(withSpec)
    }

    override suspend fun addEmoji(reactionEmoji: ReactionEmoji) {
        channel.suspendGetMessageById(event.id)?.suspendAddReaction(reactionEmoji)
    }

    override suspend fun getGuildOwnerId() = guild?.ownerId

    override suspend fun getGuildId() = guild?.id

    override fun getRoleIds() = member.roleIds

    override val isDm = guild == null

    override suspend fun joinVoiceChannel(with: VoiceChannelJoinSpec.() -> Unit) {
        member.suspendVoiceState()?.suspendChannel()?.join(with)?.subscribe()
    }

}