package com.kyledahlin.myrulebot.corona

import com.kyledahlin.rulebot.DiscordWrapper
import com.kyledahlin.rulebot.bot.GetDiscordWrapperForEvent
import com.kyledahlin.rulebot.bot.LocalStorage
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

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

class CoronaRuleTest {

    private lateinit var _corona: CoronaRule
    private lateinit var _mockStorage: LocalStorage
    private lateinit var _mockCache: GetDiscordWrapperForEvent

    @BeforeEach
    fun setup() {
        _mockCache = mock()
        _mockStorage = mock()
        _corona = CoronaRule(_mockStorage, _mockCache)
    }

    @Test
    fun `corona should respond to the trigger`() = runBlocking {
        val valid = CoronaRule.getValidTestEvent()
        val mockWrapper: DiscordWrapper = mock()
        whenever(_mockCache.invoke(any())).thenReturn(mockWrapper)

        val wasHandled = _corona.handleEvent(valid)
        assert(wasHandled)
        verify(mockWrapper).sendMessage(any<String>())
    }
}