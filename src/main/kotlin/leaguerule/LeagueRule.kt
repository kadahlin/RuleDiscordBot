package leaguerule

import Rule
import discord4j.core.`object`.entity.Message
import getTokenFromFile
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
class LeagueRule : Rule("LeagueRank") {

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
            val leagueMessage = getFirstRankString(summonerId)
            if (leagueMessage != null) {
                message.channel
                    .flatMap { it.createMessage(leagueMessage) }
                    .subscribe()
            }
        }
    }

    private suspend fun getSummonerId(leagueUsername: String): String {
        val newRequest = summonerRequest.replace(SUMMONER_NAME, leagueUsername)
        val request = newRequest + leagueApiKey
        logDebug("summoner request is $request")
        val summonerResponse = client.get<Summoner>(newRequest + leagueApiKey)
        logDebug("summonerIf for $leagueUsername is ${summonerResponse.id}")
        return summonerResponse.id
    }

    private suspend fun getFirstRankString(summonerId: String): String? {
        val newRequest = rankRequest.replace(SUMMONER_ID, summonerId)
        val ranks = client.get<RankedLeagueList>(newRequest + leagueApiKey).leagues
        logDebug("there were ${ranks.size} ranks returned for $summonerId")
        if (ranks.isEmpty()) return "This player has no ranked positions"

        val firstRank = ranks[0]
        return "${firstRank.summonerName} is currently ${firstRank.tier.toLowerCase().capitalize()} ${firstRank.rank}"
    }

    private val client
        get() = HttpClient(Apache) {
            install(JsonFeature) {
                serializer = KotlinxSerializer()
            }
        }
}