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
package com.kyledahlin.myrulebot.bot.rockpaperscissorsrule

import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.QueryDocumentSnapshot
import com.google.cloud.firestore.QuerySnapshot
import com.kyledahlin.myrulebot.bot.MyRuleBotScope
import com.kyledahlin.myrulebot.bot.suspend
import com.kyledahlin.rulebot.bot.Logger
import discord4j.common.util.Snowflake
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named

private const val PARTICIPANT_ONE = "participant1"
private const val PARTICIPANT_TWO = "participant2"
private const val WINNER = "winner"
private const val DRAW = "draw"
private const val GUILD_ID = "guildId"

@MyRuleBotScope
class RockPaperScissorsStorage @Inject constructor(
    firestore: Firestore,
    @Named("storage") val context: CoroutineDispatcher
) {

    private val _collection = firestore.collection("rps")

    suspend fun insertRpsGame(guildId: Snowflake, rpsGame: RockPaperScissorGame) {
        withContext(context) {
            val updateTime = _collection
                .add(rpsGame.toMap(guildId))
                .suspend()
            Logger.logError("update time for rps was $updateTime")
        }
    }

    suspend fun getAllRpsGamesForPlayer(snowflake: Snowflake): Collection<RockPaperScissorGame> = withContext(context) {
        val oneDocs = _collection
            .whereEqualTo("participant1", snowflake.asString())
            .get()
            .suspend<QuerySnapshot>()
            .documents

        val twoDocs = _collection
            .whereEqualTo("participant2", snowflake.asString())
            .get()
            .suspend<QuerySnapshot>()
            .documents

        val docs = oneDocs + twoDocs
        docs.mapNotNull {
            RockPaperScissorGame.fromMap(it.data)
        }
    }
}

data class RockPaperScissorGame(
    val participant1: Snowflake,
    val participant2: Snowflake,
    val winner: Snowflake,
    val draw: Boolean
) {
    companion object {
        fun fromMap(map: Map<String, Any>): RockPaperScissorGame? {
            return try {
                RockPaperScissorGame(
                    map[PARTICIPANT_ONE].sf(),
                    map[PARTICIPANT_TWO].sf(),
                    map[WINNER].sf(),
                    map[DRAW] as Boolean
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    fun toMap(guildId: Snowflake): Map<String, Any> {
        return mapOf(
            GUILD_ID to guildId.asString(),
            PARTICIPANT_ONE to participant1.asString(),
            PARTICIPANT_TWO to participant2.asString(),
            WINNER to winner.asString(),
            DRAW to draw
        )
    }
}

internal fun Any?.sf() = Snowflake.of(this as String)