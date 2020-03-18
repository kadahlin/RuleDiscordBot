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
package com.kyledahlin.myrulebot.marxrule

import com.kyledahlin.rulebot.DiscordWrapper
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class MarxRuleTest {

    private lateinit var _wrapper: DiscordWrapper

    @BeforeEach
    fun setup() {
        _wrapper = mock()
    }

    @Test
    fun `marx rule should respond to the trigger`() = runBlocking {
        val rule = MarxPassageRule(mock()) { _wrapper }
        assert(rule.handleEvent(MarxPassageRule.getTestValidEvent()))
        verify(_wrapper).sendMessage(any<String>())
    }

    @Test
    fun `mark rule should not respond to an invalid message`() = runBlocking {
        val rule = MarxPassageRule(mock()) { _wrapper }
        assert(!rule.handleEvent(MarxPassageRule.getTestInvalidEvent()))
        verify(_wrapper, never()).sendMessage(any<String>())
    }
}