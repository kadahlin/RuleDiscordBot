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
import com.kyledahlin.rulebot.bot.RuleBotScope
import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.entity.MessageChannel
import discord4j.core.`object`.util.Snowflake
import java.util.*
import javax.inject.Inject

private const val CACHE_SIZE = 20

/**
 * Hold information about things that do not persist between RuleBot runs (wrappers, members, guilds)
 */
@RuleBotScope
class DiscordCache @Inject constructor() {

    private val _messages: Deque<EventWrapper> = ArrayDeque()
    private val _botIds = mutableSetOf<Snowflake>()
    private val _guilds = mutableSetOf<GuildWrapper>()

    fun createEventWrapperEntry(event: RuleBotEvent, channel: MessageChannel, guild: Guild, member: Member) {
        if (_messages.size > CACHE_SIZE) {
            _messages.pollFirst()
        }
        _messages.add(EventWrapperImpl(event, channel, guild, member))
    }

    fun createGuildWrapperEntry(guild: Guild) {
        _guilds.add(GuildWrapperImpl(guild))
    }

    fun getGuildWrapper(snowflake: Snowflake): GuildWrapper? {
        return _guilds.firstOrNull { it.id == snowflake }
    }

    fun getWrapperForEvent(event: RuleBotEvent): EventWrapper? {
        return _messages.firstOrNull { it.event === event }
    }

    fun addBotId(snowflake: Snowflake) {
        _botIds.add(snowflake)
    }

    fun getBotIds(): Set<Snowflake> = _botIds
}

inline class AttachmentUrl(val url: String)