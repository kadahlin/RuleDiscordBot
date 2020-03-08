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
package com.kyledahlin.rulebot.bot

import com.kyledahlin.rulebot.bot.rockpaperscissorsrule.RockPaperScissorGames
import com.kyledahlin.rulebot.bot.scoreboard.ScoreboardPlayers
import com.kyledahlin.rulebot.bot.scoreboard.Scoreboards
import com.kyledahlin.rulebot.bot.timeoutrule.Timeouts
import discord4j.core.`object`.util.Snowflake
import kotlinx.coroutines.CoroutineDispatcher
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.CoroutineContext

object Admins : IntIdTable() {
    val snowflake = varchar("snowflake", 64)
    val guildSnowflake = varchar("guild_snowflake", 64)
    val isRole = bool("isRole")
}

internal interface LocalStorage {
    suspend fun getAdminSnowflakes(): Collection<RoleSnowflake>
    suspend fun addAdminSnowflakes(snowflakes: Collection<RoleSnowflake>)
    suspend fun removeAdminSnowflakes(snowflakes: Collection<RoleSnowflake>, guildId: Snowflake)
}

internal class LocalStorageImpl @Inject constructor(@Named("storage") val context: CoroutineDispatcher) : LocalStorage {

    init {
        Database.connect("jdbc:h2:./rulebot;DB_CLOSE_DELAY=-1;", "org.h2.Driver")
        TransactionManager.manager.defaultIsolationLevel =
            Connection.TRANSACTION_SERIALIZABLE // Or Connection.TRANSACTION_READ_UNCOMMITTED

        transaction {
            SchemaUtils.create(Admins)
            SchemaUtils.create(Timeouts)
            SchemaUtils.create(Scoreboards)
            SchemaUtils.create(ScoreboardPlayers)
            SchemaUtils.create(RockPaperScissorGames)
            SchemaUtils.createMissingTablesAndColumns(Admins)
        }
    }

    override suspend fun getAdminSnowflakes(): Collection<RoleSnowflake> = newSuspendedTransaction(context) {
        Admins
            .selectAll()
            .map {
                val guildIdString = it[Admins.guildSnowflake]
                val guildSnowflake = if (guildIdString.isEmpty()) {
                    null
                } else {
                    Snowflake.of(guildIdString)
                }

                RoleSnowflake(
                    Snowflake.of(it[Admins.snowflake]),
                    guildSnowflake,
                    it[Admins.isRole]
                )
            }
    }

    override suspend fun addAdminSnowflakes(snowflakes: Collection<RoleSnowflake>) = newSuspendedTransaction(context) {
        snowflakes.forEach { roleSnowflake ->
            Admins.insert {
                it[snowflake] = roleSnowflake.snowflake.asString()
                it[isRole] = roleSnowflake.isRole
                it[guildSnowflake] = roleSnowflake.guildSnowflake?.asString() ?: ""
            }
            Unit
        }
    }

    override suspend fun removeAdminSnowflakes(snowflakes: Collection<RoleSnowflake>, guildId: Snowflake) =
        newSuspendedTransaction<Unit>(context) {
            Admins.deleteWhere {
                (Admins.snowflake inList snowflakes.map { it.snowflake.asString() }) and (Admins.guildSnowflake eq guildId.asString())
            }
        }
}

