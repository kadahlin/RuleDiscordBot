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
package com.kyledahlin.myrulebot.corona

import com.kyledahlin.rulebot.DiscordWrapper
import com.kyledahlin.rulebot.bot.*
import io.ktor.client.request.get
import it.skrape.core.htmlDocument
import it.skrape.extract
import it.skrape.selects.eachText
import it.skrape.selects.html5.div
import it.skrape.skrape
import java.text.DecimalFormat
import javax.inject.Inject

private const val TRIGGER = "!corona"
private const val CASE_WEBSITE = "https://www.worldometers.info/coronavirus/"

class CoronaRule @Inject constructor(
    localStorage: LocalStorage,
    val getDiscordWrapperForEvent: GetDiscordWrapperForEvent
) : Rule("Corona", localStorage, getDiscordWrapperForEvent) {

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

    private suspend fun postStats(wrapper: DiscordWrapper) {
        val (cases, deaths) = getCasesAndDeaths() ?: 0L to 0L
        if (cases == 0L && deaths == 0L) {
            wrapper.sendMessage("unable to get corona data, either the virus is cured or this rule is broke")
            return
        }

        val rate: Double = if (cases == 0L) 0.0 else (deaths / cases.toDouble()) * 100
        wrapper.sendMessage(
            "At this moment there are ${cases.toFormatString()} cases with ${deaths.toFormatString()} deaths (${rate.toFormatString()}% mortality rate)"
        )
    }

    private suspend fun getCasesAndDeaths(): Pair<Long, Long>? {
        val htmlContent = client.get<String>(CASE_WEBSITE)
        var result: Pair<Long, Long>? = null
        try {
            htmlDocument(htmlContent) {
                div {
                    withClass = "maincounter-number"
                    val mainCounters = findAll {
                        take(3)
                            .map { it.html }
                            .map {
                                val endFirstSpan = it.indexOf(">")
                                val startSecondSpan = it.indexOf("<", startIndex = endFirstSpan)
                                it.substring(endFirstSpan + 1, startSecondSpan).trim().replace(",", "")
                            }
                    }.map { it.toLong() }
                    logDebug("parsed: $mainCounters")
                    result = mainCounters[0] to mainCounters[1]
                }
            }
        } catch (e: Exception) {
            logError("error while parsing worldinfo, ${e.message}")
        }
        return result
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
