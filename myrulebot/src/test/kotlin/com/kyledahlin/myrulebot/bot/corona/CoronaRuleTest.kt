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
import com.kyledahlin.myrulebot.TestChatInputInteractionContext
import com.kyledahlin.myrulebot.bot.RuleBaseTest
import com.kyledahlin.myrulebot.builders.hasContent
import com.kyledahlin.myrulebot.builders.isNamed
import com.kyledahlin.myrulebot.builders.isSlashCommand
import com.kyledahlin.myrulebot.testGuildCreation
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import strikt.api.expectThat

class CoronaRuleTest : RuleBaseTest() {

    private lateinit var _corona: CoronaRule

    @MockK
    private lateinit var _api: CoronaApi

    override fun init() {
        _corona = CoronaRule(_api, analytics)
    }

    @Test
    fun `corona should respond to the trigger`(): Unit = runBlocking {
        coEvery { _api.getCasesAndDeaths() } returns ((100L to 50L).right())
        val context = TestChatInputInteractionContext()
        _corona.onSlashCommand(context)
        expectThat(context.replies.first())
            .hasContent("At this moment there are 100.00 cases with 50.00 deaths (50.00% mortality rate)")
    }

    @Test
    fun `corona should register its command on guild create`(): Unit = runBlocking {
        val request = testGuildCreation(_corona).first()
        expectThat(request)
            .isNamed("corona")
            .isSlashCommand()
    }
}