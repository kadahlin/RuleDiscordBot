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
import discord4j.core.`object`.util.Snowflake
import kotlinx.coroutines.CoroutineDispatcher
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq
import javax.inject.Inject
import javax.inject.Named

interface ReactionStorage {
    suspend fun storeReactionForMember(member: Snowflake, guild: Snowflake, reaction: Snowflake)

    suspend fun removeReactionForMember(member: Snowflake, guild: Snowflake, reaction: Snowflake)

    suspend fun getReactionsForMember(guild: Snowflake, member: Snowflake): List<Snowflake>

    suspend fun getStoredReactions(guildId: Snowflake): List<StoredReaction>
}

@MyRuleBotScope
class ReactionStorageImpl @Inject constructor(
    private val database: CoroutineDatabase,
    @Named("storage") private val context: CoroutineDispatcher
) : ReactionStorage {

    private val _collection = database.getCollection<StoredReaction>()

    override suspend fun storeReactionForMember(member: Snowflake, guild: Snowflake, reaction: Snowflake) {
        _collection.insertOne(StoredReaction(member, guild, reaction))
    }

    override suspend fun removeReactionForMember(member: Snowflake, guild: Snowflake, reaction: Snowflake) {
        _collection.deleteOne(
            StoredReaction::member eq member,
            StoredReaction::guild eq guild,
            StoredReaction::emoji eq reaction,
        )
    }

    override suspend fun getReactionsForMember(guild: Snowflake, member: Snowflake): List<Snowflake> {
        return _collection.find(StoredReaction::guild eq guild, StoredReaction::member eq member)
            .toList()
            .map { it.emoji }
    }

    override suspend fun getStoredReactions(guildId: Snowflake): List<StoredReaction> {
        return _collection.find(StoredReaction::guild eq guildId).toList()
    }
}

data class StoredReaction(
    val member: Snowflake,
    val guild: Snowflake,
    val emoji: Snowflake
)