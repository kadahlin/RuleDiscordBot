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
package bot

import bot.Rule
import discord4j.core.`object`.entity.Message
import bot.getTokenFromFile
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import reactor.core.publisher.Mono

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
internal class LeagueRule : Rule("LeagueRank") {

    private val leagueApiKey by lazy {
        getTokenFromFile("leagueapi.txt")
    }

    override fun handleRule(message: Message): Mono<Boolean> {
        val isLeagueMessage = message.containsLeagueRankRule()
        if (isLeagueMessage) {
            printLeagueRankFrom(message)
        }
        return Mono.just(isLeagueMessage)
    }

    override fun getExplanation(): String? {
        return StringBuilder().apply {
            append("Get the league of legends rank of the given player\n")
            append("To use post a message with the phrase 'league rank of <league username>'\n")
        }.toString()
    }

    private fun Message.containsLeagueRankRule(): Boolean {
        val content = content.get()
        return leagueRegex.containsMatchIn(content)
    }

    private fun printLeagueRankFrom(message: Message) {
        GlobalScope.launch(Dispatchers.IO) {
            val content = message.content.get()
            val firstMatch = leagueRegex.find(content)?.value!!
            val username = firstMatch.split("\\s+".toRegex()).last()
            val summonerId = getSummonerId(username)
            if (summonerId == null) {
                message.channel
                    .flatMap { it.createMessage("this player doesn't seem to exist") }
                    .subscribe()
            } else {
                val leagueMessage = getFirstRankString(summonerId)
                if (leagueMessage != null) {
                    message.channel
                        .flatMap { it.createMessage(leagueMessage) }
                        .subscribe()
                }
            }
        }
    }

    private suspend fun getSummonerId(leagueUsername: String): String? {
        val newRequest = summonerRequest.replace(SUMMONER_NAME, leagueUsername)
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
        val newRequest = rankRequest.replace(SUMMONER_ID, summonerId)
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

    private val client
        get() = HttpClient(Apache) {
            expectSuccess = false
            install(JsonFeature) {
                serializer = KotlinxSerializer()
            }
        }
}