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
package com.kyledahlin.rulebot.bot.java

import com.kyledahlin.rulebot.DiscordWrapper
import com.kyledahlin.rulebot.bot.GetDiscordWrapperForEvent
import com.kyledahlin.rulebot.bot.RuleBotEvent
import com.kyledahlin.rulebot.bot.RuleBotScope
import javax.inject.Inject
import javax.inject.Singleton

interface JavaEventStorage : GetDiscordWrapperForEvent {
    fun getDiscordWrapperForEvent(event: RuleBotEvent): JavaDiscordWrapper?
}

@RuleBotScope
class JavaEventStorageImpl @Inject constructor(
    private val wrapperFunction: GetDiscordWrapperForEvent
) : JavaEventStorage {

    override fun invoke(event: RuleBotEvent): DiscordWrapper? = wrapperFunction(event)

    override fun getDiscordWrapperForEvent(event: RuleBotEvent): JavaDiscordWrapper? {
        val ktWrapper = wrapperFunction(event)
        return if (ktWrapper == null) null else JavaDiscordWrapperImpl(ktWrapper)
    }
}