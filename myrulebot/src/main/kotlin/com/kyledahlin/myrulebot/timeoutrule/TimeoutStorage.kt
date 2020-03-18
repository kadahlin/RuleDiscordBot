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
package com.kyledahlin.myrulebot.timeoutrule

import com.kyledahlin.myrulebot.MyRuleBotScope
import discord4j.core.`object`.util.Snowflake
import kotlinx.coroutines.CoroutineDispatcher
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Named

internal object Timeouts : IntIdTable() {
    val snowflake = varchar("snowflake", 64)
    val startTime = long("startTime")
    val duration = long("duration")
}

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
internal class TimeoutStorage @Inject constructor(private val _db: Database, @Named("storage") val context: CoroutineDispatcher) {
    suspend fun insertTimeouts(timeouts: Collection<Timeout>) = newSuspendedTransaction(context, _db) {
        for (timeout in timeouts) {
            Timeouts.insert {
                it[Timeouts.snowflake] = timeout.snowflake.asString()
                it[Timeouts.duration] = timeout.minutes
                it[Timeouts.startTime] = timeout.startTime
            }
        }
    }

    suspend fun getTimeoutForSnowflake(snowflake: Snowflake): Timeout? = newSuspendedTransaction(context, _db) {
        val existing = Timeouts
            .select { Timeouts.snowflake eq snowflake.asString() }
            .firstOrNull() ?: return@newSuspendedTransaction null

        Timeout(
            snowflake,
            existing[Timeouts.startTime],
            existing[Timeouts.duration]
        )
    }

    suspend fun removeTimeoutForSnowflakes(snowflakes: Collection<Snowflake>) = newSuspendedTransaction(context, _db) {
        Timeouts.deleteWhere {
            Timeouts.snowflake inList snowflakes.map { it.asString() }
        }
    }
}