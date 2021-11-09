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

import com.kyledahlin.rulebot.Analytics
import com.kyledahlin.rulebot.ChatInputInteractionContext
import com.kyledahlin.rulebot.GuildCreateContext
import com.kyledahlin.rulebot.bot.Logger.logError
import com.kyledahlin.rulebot.bot.Rule
import com.kyledahlin.rulebot.bot.RuleBotEvent
import discord4j.discordjson.json.ApplicationCommandRequest
import java.text.DecimalFormat
import javax.inject.Inject

class CoronaRule @Inject constructor(
    private val coronaApi: CoronaApi,
    private val analytics: Analytics
) : Rule("Corona") {

    override suspend fun handleEvent(event: RuleBotEvent): Boolean {
        return false
    }

    override fun handlesCommand(name: String): Boolean {
        return name.equals(ruleName, ignoreCase = true)
    }

    override suspend fun onSlashCommand(context: ChatInputInteractionContext) = measureExecutionTime("corona stats") {
        coronaApi.getCasesAndDeaths().fold({ e ->
            analytics.logRuleFailed(ruleName, "error while parsing worldinfo: ${e.message}")
            logError { "error while parsing worldinfo, ${e.message}" }
            context.reply {
                content { "Unable to get corona data. Either the virus is cured, the website is broke, or Kyle is a dumbass" }
            }
        }, { (cases, deaths) ->
            val rate: Double = (deaths / cases.toDouble()) * 100
            context.reply {
                content { "At this moment there are ${cases.toFormatString()} cases with ${deaths.toFormatString()} deaths (${rate.toFormatString()}% mortality rate)" }
            }
        })
    }

    override val priority: Priority
        get() = Priority.NORMAL

    override suspend fun onGuildCreate(context: GuildCreateContext) {
        super.onGuildCreate(context)
        // Build our command's definition
        val greetCmdRequest = ApplicationCommandRequest.builder()
            .name("corona")
            .description("Post plandemic stats")
            .build()

        context.registerApplicationCommand(greetCmdRequest)
    }
}

private fun Number.toFormatString(): String {
    return DecimalFormat("#,###.00").format(this)
}
