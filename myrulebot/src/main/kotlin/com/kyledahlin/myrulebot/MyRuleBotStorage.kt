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
package com.kyledahlin.myrulebot

import com.kyledahlin.myrulebot.rockpaperscissorsrule.RockPaperScissorGames
import com.kyledahlin.myrulebot.scoreboard.ScoreboardPlayers
import com.kyledahlin.myrulebot.scoreboard.Scoreboards
import com.kyledahlin.myrulebot.timeoutrule.Timeouts
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

internal object MyRuleBotStorage {

    fun create() {
//        Database.connect("jdbc:h2:./myrulebot;DB_CLOSE_DELAY=-1;", "org.h2.Driver")
//        TransactionManager.manager.defaultIsolationLevel =
//            Connection.TRANSACTION_SERIALIZABLE // Or Connection.TRANSACTION_READ_UNCOMMITTED

        transaction {
            SchemaUtils.create(Timeouts)
            SchemaUtils.create(Scoreboards)
            SchemaUtils.create(ScoreboardPlayers)
            SchemaUtils.create(RockPaperScissorGames)
        }
    }
}