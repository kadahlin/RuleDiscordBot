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
package com.kyledahlin.myrulebot.bot

import com.kyledahlin.myrulebot.bot.rockpaperscissorsrule.RockPaperScissorGames
import com.kyledahlin.myrulebot.bot.scoreboard.ScoreboardPlayers
import com.kyledahlin.myrulebot.bot.scoreboard.Scoreboards
import com.kyledahlin.myrulebot.bot.timeoutrule.Timeouts
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection

internal object MyRuleBotStorage {

    fun getDatabase(): Database {
        val db = Database.connect("jdbc:h2:./myrulebot;DB_CLOSE_DELAY=-1;", "org.h2.Driver")
        TransactionManager.manager.defaultIsolationLevel =
            Connection.TRANSACTION_SERIALIZABLE // Or Connection.TRANSACTION_READ_UNCOMMITTED

        transaction(db) {
            SchemaUtils.create(Timeouts)
            SchemaUtils.create(Scoreboards)
            SchemaUtils.create(ScoreboardPlayers)
            SchemaUtils.create(RockPaperScissorGames)
        }
        return db
    }
}