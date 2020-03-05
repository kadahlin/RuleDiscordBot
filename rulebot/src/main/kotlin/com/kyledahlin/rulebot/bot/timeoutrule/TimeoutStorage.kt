package com.kyledahlin.rulebot.bot.timeoutrule

import discord4j.core.`object`.util.Snowflake
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

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

@Singleton
internal class TimeoutStorage @Inject constructor() {
    suspend fun insertTimeouts(timeouts: Collection<Timeout>) = newSuspendedTransaction {
        for (timeout in timeouts) {
            Timeouts.insert {
                it[snowflake] = timeout.snowflake.asString()
                it[duration] = timeout.minutes
                it[startTime] = timeout.startTime
            }
        }
    }

    suspend fun getTimeoutForSnowflake(snowflake: Snowflake): Timeout? = newSuspendedTransaction {
        val existing = Timeouts.select { Timeouts.snowflake eq snowflake.asString() }
            .firstOrNull() ?: return@newSuspendedTransaction null

        Timeout(
            snowflake,
            existing[Timeouts.startTime],
            existing[Timeouts.duration]
        )
    }

    suspend fun removeTimeoutForSnowflakes(snowflakes: Collection<Snowflake>) = newSuspendedTransaction {
        Timeouts.deleteWhere {
            Timeouts.snowflake inList snowflakes.map { it.asString() }
        }
    }
}