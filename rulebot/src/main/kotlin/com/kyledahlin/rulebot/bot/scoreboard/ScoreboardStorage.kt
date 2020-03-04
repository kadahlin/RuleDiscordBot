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
package com.kyledahlin.rulebot.bot.scoreboard

import discord4j.core.`object`.util.Snowflake
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScoreboardStorage @Inject constructor() {
    fun getScoreboardIdForName(name: String): Int? = transaction {
        val query = Scoreboards.slice(Scoreboards.id)
            .select { Scoreboards.name eq name }
            .firstOrNull()

        query?.get(Scoreboards.id)
    }

    fun insertScoreboard(name: String, author: Snowflake) {
        Scoreboards.insert {
            it[Scoreboards.name] = name
            it[ownerSnowflake] = author.asString()
        }
    }

    fun getPlayersForScoreboard(scoreboardId: Int): Collection<Pair<String, Int>> = transaction {
        val query = ScoreboardPlayers.select {
            ScoreboardPlayers.scoreboardId eq scoreboardId
        }
        query.map { Pair(it[ScoreboardPlayers.name], it[ScoreboardPlayers.wins]) }
    }

    fun doesScoreboardHavePlayer(scoreboardId: Int, playerName: String): Boolean = transaction {
        ScoreboardPlayers.select {
            ScoreboardPlayers.scoreboardId eq scoreboardId and (ScoreboardPlayers.name eq playerName)
        }.count() != 0
    }

    fun addPlayer(scoreboardId: Int, playerName: String, wins: Int = 0) = transaction {
        ScoreboardPlayers.insert {
            it[ScoreboardPlayers.name] = playerName
            it[ScoreboardPlayers.wins] = wins
            it[ScoreboardPlayers.scoreboardId] = scoreboardId
        }
    }

    fun giveWinToPlayer(scoreboardId: Int, playerName: String) {
        ScoreboardPlayers.update({ ScoreboardPlayers.scoreboardId eq scoreboardId and (ScoreboardPlayers.name eq playerName) }) {
            with(SqlExpressionBuilder) {
                it.update(ScoreboardPlayers.wins, ScoreboardPlayers.wins + 1)
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