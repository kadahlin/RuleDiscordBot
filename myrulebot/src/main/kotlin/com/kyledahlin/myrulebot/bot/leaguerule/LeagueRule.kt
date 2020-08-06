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
package com.kyledahlin.myrulebot.bot.leaguerule

import com.kyledahlin.myrulebot.bot.MyRuleBotScope
import com.kyledahlin.rulebot.bot.*
import io.ktor.client.request.get
import kotlinx.serialization.json.JsonObject
import javax.inject.Inject

private val leagueRegex = "league rank of [a-zA-Z0-9]+".toRegex()
private const val SUMMONER_NAME = "SUMMONER_NAME"
private const val SUMMONER_ID = "SUMMONER_ID"

private const val rankRequest =
    "https://na1.api.riotgames.com/lol/league/v4/positions/by-summoner/$SUMMONER_ID?api_key="
private const val summonerRequest =
    "https://na1.api.riotgames.com/lol/summoner/v4/summoners/by-name/$SUMMONER_NAME?api_key="

/**
 * Print the league of legends rank for a given player
 */
@MyRuleBotScope
internal class LeagueRule @Inject constructor(
    storage: LocalStorage,
    val getDiscordWrapperForEvent: GetDiscordWrapperForEvent
) :
    Rule("LeagueRank", storage, getDiscordWrapperForEvent) {

    override val priority: Priority
        get() = Priority.LOW

    private val leagueApiKey by lazy {
        getStringFromResourceFile("RGAPI-ef950e26-958e-4427-bc9e-3f330dd7934f")
    }

    override suspend fun handleEvent(event: RuleBotEvent): Boolean {
        if (event !is MessageCreated) return false
        val isLeagueMessage = event.containsLeagueRankRule()
        if (isLeagueMessage) {
            printLeagueRankFrom(event)
        }
        return isLeagueMessage
    }

    override fun getExplanation(): String? {
        return StringBuilder().apply {
            append("Get the league of legends rank of the given player\n")
            append("To use post a message with the phrase 'league rank of <league username>'\n")
        }.toString()
    }

    private fun MessageCreated.containsLeagueRankRule() = com.kyledahlin.myrulebot.bot.leaguerule.leagueRegex.containsMatchIn(content)

    private suspend fun printLeagueRankFrom(event: MessageCreated) {
        val wrapper = getDiscordWrapperForEvent(event) ?: return
        val firstMatch = leagueRegex.find(event.content)?.value!!
        val username = firstMatch.split("\\s+".toRegex()).last()
        val summonerId = getSummonerId(username)
        if (summonerId == null) {
            wrapper.sendMessage("this player doesn't seem to exist")
        } else {
            val leagueMessage = getFirstRankString(summonerId)
            if (leagueMessage != null) {
                wrapper.sendMessage(leagueMessage)
            }
        }
    }

    private suspend fun getSummonerId(leagueUsername: String): String? {
        val newRequest = summonerRequest.replace(
            SUMMONER_NAME, leagueUsername
        )
        val request = newRequest + leagueApiKey
        logDebug("summoner request is $request")
        val summonerResponse = try {
            client.get<Summoner>(newRequest + leagueApiKey)
        } catch (e: Exception) {
            logError("could not get summonerId for $leagueUsername")
            return null
        }
        logDebug("summonerId for $leagueUsername is ${summonerResponse.id}")
        return summonerResponse.id
    }

    private suspend fun getFirstRankString(summonerId: String): String? {
        val newRequest = rankRequest.replace(
            SUMMONER_ID, summonerId
        )
        val ranks = try {
            client.get<RankedLeagueList>(newRequest + leagueApiKey).leagues
        } catch (e: Exception) {
            logError("could not get ranks for $summonerId")
            emptyList<RankedLeague>()
        }
        logDebug("there were ${ranks.size} ranks returned for $summonerId")
        if (ranks.isEmpty()) return "This player has no ranked positions"

        val firstRank = ranks[0]
        return "${firstRank.summonerName} is currently ${firstRank.tier.toLowerCase().capitalize()} ${firstRank.rank}"
    }

    override suspend fun configure(data: Any): Any {
        return JsonObject(mapOf())
    }
}