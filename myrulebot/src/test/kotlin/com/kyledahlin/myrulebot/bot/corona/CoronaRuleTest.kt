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
package com.kyledahlin.myrulebot.bot.corona

import arrow.core.right
import com.kyledahlin.myrulebot.bot.RuleBaseTest
import com.kyledahlin.rulebot.EventWrapper
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class CoronaRuleTest : RuleBaseTest() {

    private lateinit var _corona: CoronaRule
    private lateinit var _api: CoronaApi

    override fun init() {
        _api = mock()
        _corona = CoronaRule(_api, getWrapper, analytics)
    }

    @Test
    fun `corona should respond to the trigger`() = runBlocking {
        val valid = CoronaRule.getValidTestEvent()
        val mockWrapper: EventWrapper = mock()
        addEvent(valid, mockWrapper)
        whenever(_api.getCasesAndDeaths()).thenReturn((1L to 1L).right())

        val wasHandled = _corona.handleEvent(valid)
        assertThat(wasHandled).isTrue
        verify(mockWrapper).sendMessage(any<String>())
    }
}