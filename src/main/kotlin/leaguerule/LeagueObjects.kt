package leaguerule

import kotlinx.serialization.*
import kotlinx.serialization.internal.StringDescriptor

//Objects representing JSON that is returned from the league of legends api

@Serializable
internal data class Summoner(
    val id: String,
    val accountId: String,
    val puuid: String,
    val name: String,
    val profileIconId: Long,
    val revisionDate: Long,
    val summonerLevel: Int
)

@Serializable
class RankedLeagueList(
    val leagues: List<RankedLeague>
) {

    @Serializer(RankedLeagueList::class)
    companion object : KSerializer<RankedLeagueList> {

        override val descriptor = StringDescriptor.withName("RankedLeagueList")

        override fun serialize(encoder: Encoder, obj: RankedLeagueList) {
            RankedLeague.serializer().list.serialize(encoder, obj.leagues)
        }

        override fun deserialize(decoder: Decoder): RankedLeagueList {
            return RankedLeagueList(RankedLeague.serializer().list.deserialize(decoder))
        }
    }
}

@Serializable
data class RankedLeague(
    val leagueId: String,
    val leagueName: String,
    val queueType: String,
    val position: String,
    val tier: String,
    val rank: String,
    val leaguePoints: Int,
    val wins: Int,
    val losses: Int,
    val veteran: Boolean,
    val inactive: Boolean,
    val freshBlood: Boolean,
    val hotStreak: Boolean,
    val summonerId: String,
    val summonerName: String
)