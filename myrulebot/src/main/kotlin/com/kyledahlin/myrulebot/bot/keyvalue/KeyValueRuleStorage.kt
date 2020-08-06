package com.kyledahlin.myrulebot.bot.keyvalue

import com.kyledahlin.myrulebot.bot.MyRuleBotScope
import discord4j.core.`object`.util.Snowflake
import kotlinx.coroutines.CoroutineDispatcher
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import javax.inject.Inject
import javax.inject.Named

internal interface KeyValueRuleStorage {
    suspend fun getDataForGuildId(snowflake: Snowflake): TodaysGuyData?

    suspend fun addTriggerForGuild(snowflake: Snowflake, trigger: String, response: String)
}

@MyRuleBotScope
internal class KeyValueRuleStorageImpl @Inject constructor(
    private val _db: Database,
    @Named("storage") val context: CoroutineDispatcher
) : KeyValueRuleStorage {

    override suspend fun getDataForGuildId(snowflake: Snowflake): TodaysGuyData? =
        newSuspendedTransaction(context, _db) {
            KeyValueRuleTable
                .select { KeyValueRuleTable.guildId eq snowflake.asString() }
                .firstOrNull()?.let {
                    val trigger = it[KeyValueRuleTable.trigger]
                    val response = it[KeyValueRuleTable.response]
                    TodaysGuyData(snowflake, trigger, response)
                }
        }

    override suspend fun addTriggerForGuild(snowflake: Snowflake, trigger: String, response: String) =
        newSuspendedTransaction<Unit>(context, _db) {

            KeyValueRuleTable.deleteWhere {
                (KeyValueRuleTable.guildId eq snowflake.asString()).and(KeyValueRuleTable.trigger eq trigger)
            }

            KeyValueRuleTable.insert {
                it[guildId] = snowflake.asString()
                it[KeyValueRuleTable.trigger] = trigger
                it[KeyValueRuleTable.response] = response
            }
        }
}

object KeyValueRuleTable : Table() {
    val guildId = varchar("guildId", 64)
    val trigger = varchar("trigger", 64)
    val response = varchar("response", 64)

    override val primaryKey: PrimaryKey?
        get() = PrimaryKey(guildId, trigger)
}

internal data class TodaysGuyData(
    val guildId: Snowflake,
    val trigger: String,
    val repsonse: String
)