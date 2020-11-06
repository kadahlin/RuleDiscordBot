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

import com.kyledahlin.myrulebot.bot.MyRuleBotScope
import discord4j.core.`object`.util.Snowflake
import kotlinx.coroutines.CoroutineDispatcher
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq
import org.litote.kmongo.or
import javax.inject.Inject
import javax.inject.Named

@MyRuleBotScope
class RockPaperScissorsStorage @Inject constructor(
    database: CoroutineDatabase,
    @Named("storage") val context: CoroutineDispatcher
) {

    private val _collection = database.getCollection<RockPaperScissorGame>()

    suspend fun insertRpsGame(rpsGame: RockPaperScissorGame) {
        _collection.insertOne(rpsGame)
    }

    suspend fun getAllRpsGamesForPlayer(snowflake: Snowflake): Collection<RockPaperScissorGame> {
        return _collection.find(
            or(
                RockPaperScissorGame::participant1 eq snowflake,
                RockPaperScissorGame::participant2 eq snowflake
            )
        )
            .toList()
    }
}