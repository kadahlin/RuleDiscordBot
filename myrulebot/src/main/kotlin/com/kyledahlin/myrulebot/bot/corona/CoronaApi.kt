package com.kyledahlin.myrulebot.bot.corona

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.kyledahlin.rulebot.bot.client
import io.ktor.client.request.*
import it.skrape.core.htmlDocument
import it.skrape.selects.html5.div
import javax.inject.Inject

private const val CASE_WEBSITE = "https://www.worldometers.info/coronavirus/"

//Fetch the number of corona cases and deaths
open class CoronaApi @Inject constructor(){
    open suspend fun getCasesAndDeaths(): Either<Exception, Pair<Long, Long>> {
        val htmlContent = client.get<String>(CASE_WEBSITE)
        var result: Either<Exception, Pair<Long, Long>> = IllegalStateException().left()
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
                    if (mainCounters[0] != 0L && mainCounters[1] != 0L) {
                        result = (mainCounters[0] to mainCounters[1]).right()
                    }
                }
            }
        } catch (e: Exception) {
            result = e.left()
        }
        return result
    }
}