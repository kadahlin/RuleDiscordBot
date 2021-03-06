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
package com.kyledahlin.myrulebot.bot.timeoutrule

import com.kyledahlin.myrulebot.bot.MyRuleBotScope
import discord4j.core.`object`.util.Snowflake
import kotlinx.coroutines.CoroutineDispatcher
import org.litote.kmongo.`in`
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Named

internal data class Timeout(
    val snowflake: Snowflake,
    val startTime: Long,
    val minutes: Long
) {

    override fun equals(other: Any?): Boolean {
        if (other is Timeout) {
            return other.snowflake == this.snowflake
        }
        return false
    }

    fun getFormattedEndDate(): String {
        val date = Date(startTime + (minutes * 60L * 1000L))
        val format = SimpleDateFormat("hh:mm MMMM dd, YYYY", Locale.US)
        return format.format(date)
    }
}

@MyRuleBotScope
internal class TimeoutStorage @Inject constructor(
    database: CoroutineDatabase,
    @Named("storage") val context: CoroutineDispatcher
) {

    private val collection = database.getCollection<Timeout>()

    suspend fun insertTimeouts(timeouts: Collection<Timeout>) {
        collection.insertMany(timeouts.toList())
    }

    suspend fun getTimeoutForSnowflake(snowflake: Snowflake): Timeout? {
        return collection.findOne(Timeout::snowflake eq snowflake)
    }

    suspend fun removeTimeoutForSnowflakes(snowflakes: Collection<Snowflake>) {
        collection.deleteMany(Timeout::snowflake `in` snowflakes)
    }
}