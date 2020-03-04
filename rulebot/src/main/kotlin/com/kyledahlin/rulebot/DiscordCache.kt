/*
*Copyright 2019 Kyle Dahlin
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

import com.kyledahlin.rulebot.bot.Logger
import com.kyledahlin.rulebot.bot.RuleBotEvent
import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.entity.MessageChannel
import discord4j.core.`object`.util.Snowflake
import discord4j.core.spec.MessageCreateSpec
import discord4j.core.spec.VoiceChannelJoinSpec
import suspendChannel
import suspendCreateMessage
import suspendDelete
import suspendGetMessageById
import suspendOwner
import suspendVoiceState
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

private const val CACHE_SIZE = 20

/**
 * Hold the [CACHE_SIZE] latest messages and the corresponding [MessageChannel] that they belong
 * to.
 */
@Singleton
class DiscordCache @Inject constructor() {
    private val _messages: Deque<DiscordWrapper> = ArrayDeque<DiscordWrapper>()

    fun add(event: RuleBotEvent, channel: MessageChannel, guild: Guild, member: Member) {
        Logger.logDebug("this is ${toString()}")
        if (_messages.size > CACHE_SIZE) {
            _messages.pollFirst()
        }
        _messages.add(EventMetadata(event, channel, guild, member))
    }

    fun getMetadataForEvent(event: RuleBotEvent): DiscordWrapper? {
        Logger.logDebug("this is ${toString()}")
        return _messages.firstOrNull { it.event === event }
    }
}

interface DiscordWrapper {

    val event: RuleBotEvent

    suspend fun deleteMessage()

    suspend fun sendMessage(message: String)

    suspend fun sendMessage(withSpec: MessageCreateSpec.() -> Unit)

    suspend fun getGuildOwnerId(): Snowflake?

    val isDm: Boolean

    fun getRoleIds(): Set<Snowflake>

    suspend fun joinVoiceChannel(with: VoiceChannelJoinSpec.() -> Unit)
}

private class EventMetadata(
    ruleBotEvent: RuleBotEvent,
    val channel: MessageChannel,
    val guild: Guild?,
    val member: Member
) : DiscordWrapper {

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

    override suspend fun getGuildOwnerId() = guild?.ownerId

    override fun getRoleIds() = member.roleIds

    override val isDm = guild == null

    override suspend fun joinVoiceChannel(with: VoiceChannelJoinSpec.() -> Unit) {
        member.suspendVoiceState()?.suspendChannel()?.join(with)?.subscribe()
    }

}

inline class AttachmentUrl(val url: String)