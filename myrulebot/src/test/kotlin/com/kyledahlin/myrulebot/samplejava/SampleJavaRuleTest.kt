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
package com.kyledahlin.myrulebot.samplejava

import com.kyledahlin.rulebot.bot.Logger
import com.kyledahlin.rulebot.bot.java.JavaDiscordWrapper
import com.kyledahlin.rulebot.bot.java.JavaEventStorage
import com.kyledahlin.rulebot.bot.java.JavaLocalStorage
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SampleJavaRuleTest {

    private lateinit var _rule: com.kyledahlin.myrulebot.samplejava.SampleJavaRule
    private lateinit var _mockLocalStorage: JavaLocalStorage
    private lateinit var _mockEventStorage: JavaEventStorage
    private lateinit var _mockWrapper: JavaDiscordWrapper

    @BeforeEach
    fun setup() {
        _mockEventStorage = mock()
        _mockLocalStorage = mock()
        _mockWrapper = mock()
        whenever(_mockEventStorage.getDiscordWrapperForEvent(any())).thenReturn(_mockWrapper)
        _rule = com.kyledahlin.myrulebot.samplejava.SampleJavaRule(_mockLocalStorage, _mockEventStorage)
    }

    @Test
    fun `the java rule should respond to its valid event`() = runBlocking {
        val valid = com.kyledahlin.myrulebot.samplejava.SampleJavaRule.getValidTestEvent()
        Logger.logDebug("current is $coroutineContext")
        Logger.logDebug("global is ${GlobalScope.coroutineContext}")

        _rule.handleEvent(valid)
        verify(_mockWrapper).sendMessage(any<String>())
    }

    @Test
    fun `the java rule should not respond to its invalid event`() = runBlocking {
        val invalid = com.kyledahlin.myrulebot.samplejava.SampleJavaRule.getInvalidTestEvent()
        _rule.handleEvent(invalid)
        verify(_mockWrapper, never()).sendMessage(any<String>())
    }
}