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

import com.kyledahlin.rulebot.Analytics
import com.kyledahlin.rulebot.DiscordCache
import com.kyledahlin.rulebot.EventWrapper
import com.kyledahlin.rulebot.GuildWrapper
import com.kyledahlin.rulebot.bot.GetDiscordWrapperForEvent
import com.kyledahlin.rulebot.bot.RuleBotEvent
import discord4j.common.util.Snowflake
import org.junit.jupiter.api.BeforeEach
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

abstract class RuleBaseTest {
    protected val cache: DiscordCache = mock()
    protected val analytics: Analytics = mock()
    private val _events = mutableMapOf<RuleBotEvent, EventWrapper>()
    private val _guilds = mutableMapOf<Snowflake, GuildWrapper>()

    protected val getWrapper: GetDiscordWrapperForEvent = { event ->
        _events[event]
    }

    protected fun addEvent(event: RuleBotEvent, wrapper: EventWrapper) {
        _events[event] = wrapper
    }

    protected fun addGuildWrapper(id: Snowflake, wrapper: GuildWrapper) {
        _guilds[id] = wrapper
    }

    @BeforeEach
    fun setup() {
        whenever(cache.getGuildWrapper(any())).thenAnswer {
            val snowflake = it.arguments[0] as Snowflake
            _guilds[snowflake]
        }
        init()
    }

    abstract fun init()
}