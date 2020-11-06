package com.kyledahlin.myrulebot.bot.keyvalue

import com.kyledahlin.myrulebot.bot.MyRuleBotScope
import discord4j.core.`object`.util.Snowflake
import kotlinx.coroutines.CoroutineDispatcher
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq
import javax.inject.Inject
import javax.inject.Named

internal interface KeyValueRuleStorage {
    suspend fun getDataForGuildId(snowflake: Snowflake): List<Trigger>

    suspend fun addTriggerForGuild(snowflake: Snowflake, trigger: String, response: String)
}

internal data class Trigger(
    val snowflake: Snowflake, val trigger: String, val response: String
)

@MyRuleBotScope
internal class KeyValueRuleStorageImpl @Inject constructor(
    val database: CoroutineDatabase,
    @Named("storage") val context: CoroutineDispatcher
) : KeyValueRuleStorage {

    private val _collection = database.getCollection<Trigger>()

    override suspend fun getDataForGuildId(snowflake: Snowflake): List<Trigger> {
        return _collection.find(Trigger::snowflake eq snowflake).toList()
    }

    override suspend fun addTriggerForGuild(snowflake: Snowflake, trigger: String, response: String) {

        _collection.deleteOne(
            Trigger::snowflake eq snowflake, Trigger::trigger eq trigger
        )

        _collection.insertOne(Trigger(snowflake, trigger, response))
    }
}