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

import com.kyledahlin.rulebot.RuleBot
import com.kyledahlin.rulebot.bot.DaggerBotComponent
import com.kyledahlin.rulebot.bot.LogLevel
import discord4j.core.`object`.util.Snowflake

object MyRulebot {
    fun create(token: String, rulesToLog: Collection<String>, logLevel: LogLevel): RuleBot {
        val coreComponent = DaggerBotComponent.builder().setToken(token).build()
        val myRules = DaggerMyRuleBotComponent.builder().botComponent(coreComponent).build().rules()
        val builder = coreComponent.botBuilder().apply {
            addRules(myRules)
            logRules(*rulesToLog.toTypedArray())
            this.logLevel = logLevel
        }
        return builder.build()
    }
}