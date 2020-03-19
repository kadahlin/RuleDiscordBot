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
import discord4j.core.`object`.entity.GuildEmoji
import discord4j.core.`object`.util.Snowflake
import kotlinx.coroutines.CoroutineDispatcher
import org.jetbrains.exposed.sql.Database
import javax.inject.Inject
import javax.inject.Named

interface ReactionStorage {
    suspend fun storeReactionForMember(member: Snowflake, guild: Snowflake, reaction: GuildEmoji)

    suspend fun removeReactionForMember(member: Snowflake, guild: Snowflake, reaction: GuildEmoji)
}

@MyRuleBotScope
class ReactionStorageImpl @Inject constructor(private val _db: Database, @Named("storage") private val context: CoroutineDispatcher) :
    ReactionStorage {
    override suspend fun storeReactionForMember(member: Snowflake, guild: Snowflake, reaction: GuildEmoji) {
        TODO("Not yet implemented")
    }

    override suspend fun removeReactionForMember(member: Snowflake, guild: Snowflake, reaction: GuildEmoji) {
        TODO("Not yet implemented")
    }
}