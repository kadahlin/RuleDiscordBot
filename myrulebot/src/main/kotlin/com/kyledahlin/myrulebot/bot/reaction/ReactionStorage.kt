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
package com.kyledahlin.myrulebot.bot.reaction

import com.kyledahlin.myrulebot.bot.MyRuleBotScope
import com.kyledahlin.myrulebot.bot.sf
import discord4j.core.`object`.util.Snowflake
import kotlinx.coroutines.CoroutineDispatcher
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import javax.inject.Inject
import javax.inject.Named

interface ReactionStorage {
    suspend fun storeReactionForMember(member: Snowflake, guild: Snowflake, reaction: Snowflake)

    suspend fun removeReactionForMember(member: Snowflake, guild: Snowflake, reaction: Snowflake)

    suspend fun getReactionsForMember(guild: Snowflake, member: Snowflake): List<Snowflake>
}

@MyRuleBotScope
class ReactionStorageImpl @Inject constructor(
    private val _db: Database,
    @Named("storage") private val context: CoroutineDispatcher
) : ReactionStorage {

    override suspend fun storeReactionForMember(member: Snowflake, guild: Snowflake, reaction: Snowflake) {
        newSuspendedTransaction(context, _db) {
            ReactionTable.insert {
                it[ReactionTable.member] = member.asString()
                it[ReactionTable.guild] = guild.asString()
                it[ReactionTable.reaction] = reaction.asString()
            }
        }
    }

    override suspend fun removeReactionForMember(member: Snowflake, guild: Snowflake, reaction: Snowflake) {
        newSuspendedTransaction(context, _db) {
            ReactionTable.deleteWhere {
                (ReactionTable.member eq member.asString())
                    .and(ReactionTable.guild eq guild.asString())
                    .and(ReactionTable.reaction eq reaction.asString())
            }
        }
    }

    override suspend fun getReactionsForMember(guild: Snowflake, member: Snowflake): List<Snowflake> =
        newSuspendedTransaction(context, _db) {
            ReactionTable
                .select { ReactionTable.guild eq guild.asString() and (ReactionTable.member eq member.asString()) }
                .map { it[ReactionTable.reaction].sf() }
        }
}

object ReactionTable : IntIdTable() {
    val member = ReactionTable.varchar("member", 64)
    val guild = ReactionTable.varchar("guild", 64)
    val reaction = ReactionTable.varchar("reaction", 64)
}