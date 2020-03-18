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
package com.kyledahlin.myrulebot.rockpaperscissorsrule

import com.kyledahlin.myrulebot.MyRuleBotScope
import discord4j.core.`object`.util.Snowflake
import kotlinx.coroutines.CoroutineDispatcher
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import javax.inject.Inject
import javax.inject.Named

@MyRuleBotScope
class RockPaperScissorsStorage @Inject constructor(@Named("storage") val context: CoroutineDispatcher) {
    suspend fun insertRpsGame(rpsGame: RockPaperScissorGame) = newSuspendedTransaction(context) {
        println("inserting $rpsGame")
        RockPaperScissorGames.insert {
            it[RockPaperScissorGames.participant1] = rpsGame.participant1.asString()
            it[RockPaperScissorGames.participant2] = rpsGame.participant2.asString()
            it[RockPaperScissorGames.winner] = rpsGame.winner.asString()
            it[RockPaperScissorGames.draw] = rpsGame.draw
        }
    }

    suspend fun getAllRpsGamesForPlayer(snowflake: Snowflake): Collection<RockPaperScissorGame> =
        newSuspendedTransaction(context) {
            RockPaperScissorGames
                .select { RockPaperScissorGames.participant1 eq snowflake.asString() or (RockPaperScissorGames.participant2 eq snowflake.asString()) }
                .map {
                    val participant1 = it[RockPaperScissorGames.participant1]
                    val participant2 = it[RockPaperScissorGames.participant2]
                    val winner = it[RockPaperScissorGames.winner]
                    val draw = it[RockPaperScissorGames.draw]
                    RockPaperScissorGame(
                        Snowflake.of(
                            participant1
                        ), Snowflake.of(participant2), Snowflake.of(winner), draw
                    )
                }.toList()
        }
}

object RockPaperScissorGames : IntIdTable() {
    val participant1 = varchar("participant1", 64)
    val participant2 = varchar("participant2", 64)
    val winner = varchar("winner", 64)
    val draw = bool("draw")
}