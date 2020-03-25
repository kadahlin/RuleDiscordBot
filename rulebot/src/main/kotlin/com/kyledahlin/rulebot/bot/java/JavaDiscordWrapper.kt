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
package com.kyledahlin.rulebot.bot.java

import com.kyledahlin.rulebot.EventWrapper
import discord4j.core.`object`.util.Snowflake
import discord4j.core.spec.MessageCreateSpec
import discord4j.core.spec.VoiceChannelJoinSpec
import kotlinx.coroutines.runBlocking

/**
 * Allow java based class to interact with a kotlin [EventWrapper]
 */
interface JavaDiscordWrapper {
    fun deleteMessage()

    fun sendMessage(message: String)

    fun sendMessage(withSpec: MessageCreateSpec.() -> Unit)

    fun getGuildOwnerId(): Snowflake?

    fun getGuildId(): Snowflake?

    val isDm: Boolean

    fun getRoleIds(): Set<Snowflake>

    fun joinVoiceChannel(with: VoiceChannelJoinSpec.() -> Unit)
}

class JavaDiscordWrapperImpl(private val _wrapper: EventWrapper) : JavaDiscordWrapper {
    val event = _wrapper.event

    override fun deleteMessage() = runBlocking {
        _wrapper.deleteMessage()
    }

    override fun sendMessage(message: String) = runBlocking {
        _wrapper.sendMessage(message)
    }

    override fun sendMessage(withSpec: MessageCreateSpec.() -> Unit) = runBlocking {
        _wrapper.sendMessage(withSpec)
    }

    override fun getGuildOwnerId() = runBlocking {
        _wrapper.getGuildOwnerId()
    }

    override fun getGuildId() = runBlocking {
        _wrapper.getGuildId()
    }

    override val isDm: Boolean = _wrapper.isDm

    override fun getRoleIds(): Set<Snowflake> = _wrapper.getRoleIds()

    override fun joinVoiceChannel(with: VoiceChannelJoinSpec.() -> Unit) = runBlocking {
        _wrapper.joinVoiceChannel(with)
    }
}