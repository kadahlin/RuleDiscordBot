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
package com.kyledahlin.myrulebot.scoreboard

import com.kyledahlin.myrulebot.MyRuleBotScope
import discord4j.core.`object`.util.Snowflake
import kotlinx.coroutines.CoroutineDispatcher
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@MyRuleBotScope
class ScoreboardStorage @Inject constructor(@Named("storage") val context: CoroutineDispatcher) {
    suspend fun getScoreboardIdForName(name: String): Int? = newSuspendedTransaction {
        val query = Scoreboards
            .slice(Scoreboards.id)
            .select { Scoreboards.name eq name }
            .firstOrNull()

        query?.get(Scoreboards.id)
    }

    suspend fun insertScoreboard(name: String, author: Snowflake) = newSuspendedTransaction {
        Scoreboards.insert {
            it[Scoreboards.name] = name
            it[Scoreboards.ownerSnowflake] = author.asString()
        }
    }

    suspend fun getPlayersForScoreboard(scoreboardId: Int): Collection<Pair<String, Int>> = newSuspendedTransaction {
        val query = ScoreboardPlayers.select {
            ScoreboardPlayers.scoreboardId eq scoreboardId
        }
        query.map { Pair(it[ScoreboardPlayers.name], it[ScoreboardPlayers.wins]) }
    }

    suspend fun doesScoreboardHavePlayer(scoreboardId: Int, playerName: String): Boolean = newSuspendedTransaction {
        ScoreboardPlayers.select {
            ScoreboardPlayers.scoreboardId eq scoreboardId and (ScoreboardPlayers.name eq playerName)
        }.count() != 0
    }

    suspend fun addPlayer(scoreboardId: Int, playerName: String, wins: Int = 0) = newSuspendedTransaction {
        ScoreboardPlayers.insert {
            it[ScoreboardPlayers.name] = playerName
            it[ScoreboardPlayers.wins] = wins
            it[ScoreboardPlayers.scoreboardId] = scoreboardId
        }
    }

    suspend fun giveWinToPlayer(scoreboardId: Int, playerName: String) = newSuspendedTransaction {
        ScoreboardPlayers.update({ ScoreboardPlayers.scoreboardId eq scoreboardId and (ScoreboardPlayers.name eq playerName) }) {
            with(SqlExpressionBuilder) {
                it.update(
                    com.kyledahlin.myrulebot.scoreboard.ScoreboardPlayers.wins, com.kyledahlin.myrulebot.scoreboard.ScoreboardPlayers.wins + 1)
            }
        }
    }
}

object Scoreboards : Table() {
    val id = integer("id").primaryKey().autoIncrement()
    val ownerSnowflake = varchar("snowflake", 64)
    val name = varchar("name", 30)
}

object ScoreboardPlayers : Table() {
    val name = varchar("name", 30).primaryKey(0)
    val wins = integer("wins")
    val scoreboardId = integer("scoreboard_id").references(Scoreboards.id, ReferenceOption.CASCADE).primaryKey(1)
}