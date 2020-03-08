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
package com.kyledahlin.rulebot.bot.java

import com.kyledahlin.rulebot.bot.LocalStorage
import com.kyledahlin.rulebot.bot.RoleSnowflake
import discord4j.core.`object`.util.Snowflake
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

interface JavaLocalStorage : LocalStorage {
    fun getAdminSnowflakesJ(): Collection<RoleSnowflake>

    fun addAdminSnowflakesJ(snowflakes: Collection<RoleSnowflake>)

    fun removeAdminSnowflakesJ(
        snowflakes: Collection<RoleSnowflake>,
        guildId: Snowflake
    )
}

@Singleton
class JavaLocalStorageImpl @Inject constructor(private val _localStorage: LocalStorage) : JavaLocalStorage {
    override suspend fun getAdminSnowflakes() = _localStorage.getAdminSnowflakes()

    override suspend fun addAdminSnowflakes(snowflakes: Collection<RoleSnowflake>) = _localStorage.addAdminSnowflakes(snowflakes)

    override suspend fun removeAdminSnowflakes(snowflakes: Collection<RoleSnowflake>, guildId: Snowflake) =
        _localStorage.removeAdminSnowflakes(snowflakes, guildId)

    override fun getAdminSnowflakesJ() = runBlocking {
        getAdminSnowflakes()
    }

    override fun addAdminSnowflakesJ(snowflakes: Collection<RoleSnowflake>) = runBlocking {
        addAdminSnowflakes(snowflakes)
    }

    override fun removeAdminSnowflakesJ(
        snowflakes: Collection<RoleSnowflake>,
        guildId: Snowflake
    ) = runBlocking {
        removeAdminSnowflakes(snowflakes, guildId)

    }
}