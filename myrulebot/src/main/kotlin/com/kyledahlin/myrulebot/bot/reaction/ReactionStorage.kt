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

import com.google.cloud.firestore.Firestore
import com.kyledahlin.myrulebot.bot.MyRuleBotScope
import com.kyledahlin.myrulebot.bot.rockpaperscissorsrule.sf
import com.kyledahlin.myrulebot.bot.suspend
import discord4j.common.util.Snowflake
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named

interface ReactionStorage {
    suspend fun storeReactionForMember(member: Snowflake, guild: Snowflake, reaction: Snowflake)

    suspend fun removeReactionForMember(member: Snowflake, guild: Snowflake, reaction: Snowflake)

    suspend fun getReactionsForMember(guild: Snowflake, member: Snowflake): List<Snowflake>

    suspend fun getStoredReactions(guildId: Snowflake): List<StoredReaction>
}

private const val USER_ID = "userId"
private const val GUILD_ID = "guildId"
private const val REACTION = "reaction"

@MyRuleBotScope
class ReactionStorageImpl @Inject constructor(
    firestore: Firestore,
    @Named("storage") private val context: CoroutineDispatcher
) : ReactionStorage {

    private val _collection = firestore.collection("reactions")

    override suspend fun storeReactionForMember(userId: Snowflake, guildId: Snowflake, reaction: Snowflake) {
        withContext(context) {
            _collection
                .document()
                .set(StoredReaction(userId, guildId, reaction).toMap())
                .suspend()
        }
    }

    override suspend fun removeReactionForMember(userId: Snowflake, guildId: Snowflake, reaction: Snowflake) =
        withContext(context) {
            _collection
                .whereEqualTo(USER_ID, userId.asString())
                .whereEqualTo(GUILD_ID, guildId.asString())
                .whereEqualTo(REACTION, reaction.asString())
                .get()
                .suspend()
                .documents.forEach {
                    it.reference.delete().suspend()
                }
        }

    override suspend fun getReactionsForMember(userId: Snowflake, guildId: Snowflake) = withContext(context) {
        _collection
            .whereEqualTo(USER_ID, userId.asString())
            .whereEqualTo(GUILD_ID, guildId.asString())
            .get()
            .suspend()
            .documents
            .mapNotNull { it.data[REACTION].sf() }
    }

    override suspend fun getStoredReactions(guildId: Snowflake): List<StoredReaction> = withContext(context) {
        _collection
            .whereEqualTo(GUILD_ID, guildId.asString())
            .get()
            .suspend()
            .documents
            .mapNotNull { StoredReaction.fromMap(it.data) }
    }
}

data class StoredReaction(
    val member: Snowflake,
    val guild: Snowflake,
    val emoji: Snowflake
) {
    companion object {

        fun fromMap(map: Map<String, Any>): StoredReaction? {
            return try {
                StoredReaction(
                    member = map[USER_ID].sf(),
                    guild = map[GUILD_ID].sf(),
                    emoji = map[REACTION].sf()
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    fun toMap(): Map<String, Any> {
        return mapOf(
            USER_ID to member,
            GUILD_ID to guild,
            REACTION to emoji
        )
    }
}