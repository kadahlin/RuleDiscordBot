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
import com.kyledahlin.rulebot.EventWrapper
import com.kyledahlin.rulebot.bot.GetDiscordWrapperForEvent
import com.kyledahlin.rulebot.bot.MessageCreated
import com.kyledahlin.rulebot.bot.Rule
import com.kyledahlin.rulebot.bot.RuleBotEvent
import java.text.DecimalFormat
import javax.inject.Inject

private const val TRIGGER = "!corona"

class CoronaRule @Inject constructor(
    private val coronaApi: CoronaApi,
    private val getDiscordWrapperForEvent: GetDiscordWrapperForEvent,
    private val analytics: Analytics
) : Rule("Corona", getDiscordWrapperForEvent) {

    override suspend fun handleEvent(event: RuleBotEvent): Boolean {
        if (event !is MessageCreated) return false

        val containsTrigger = event.content.contains(TRIGGER)
        val wrapper = getDiscordWrapperForEvent(event)

        return if (containsTrigger && wrapper != null) {
            postStats(wrapper)
            true
        } else {
            false
        }
    }

    private suspend fun postStats(wrapper: EventWrapper) {
        coronaApi.getCasesAndDeaths().fold({ e ->
            analytics.logRuleFailed(ruleName, "error while parsing worldinfo: ${e.message}")
            logError("error while parsing worldinfo, ${e.message}")
            wrapper.sendMessage("unable to get corona data, either the virus is cured or this rule is broke")
        }, { (cases, deaths) ->
            val rate: Double = (deaths / cases.toDouble()) * 100
            wrapper.sendMessage(
                "At this moment there are ${cases.toFormatString()} cases with ${deaths.toFormatString()} deaths (${rate.toFormatString()}% mortality rate)"
            )
        })
    }

    override fun getExplanation(): String? {
        return "Type $TRIGGER to get the latest stats on the world deadliest virus"
    }

    override val priority: Priority
        get() = Priority.NORMAL

    companion object {
        fun getValidTestEvent(): RuleBotEvent {
            return MessageCreated(content = TRIGGER)
        }

        fun getInvalidTestEvent(): RuleBotEvent {
            return MessageCreated()
        }
    }
}

private fun Number.toFormatString(): String {
    return DecimalFormat("#,###.00").format(this)
}
