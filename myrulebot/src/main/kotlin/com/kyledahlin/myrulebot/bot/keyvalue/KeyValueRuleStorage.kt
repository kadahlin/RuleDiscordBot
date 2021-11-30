package com.kyledahlin.myrulebot.bot.keyvalue

import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.SetOptions
import com.kyledahlin.myrulebot.bot.MyRuleBotScope
import com.kyledahlin.myrulebot.bot.suspend
import discord4j.common.util.Snowflake
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
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
    firestore: Firestore,
    @Named("storage") val context: CoroutineDispatcher
) : KeyValueRuleStorage {

    private val _collection = firestore.collection("keyvalue")

    override suspend fun getDataForGuildId(snowflake: Snowflake): List<Trigger> = withContext(context) {
        val document = _collection
            .document(snowflake.asString())
            .get()
            .suspend()
        val map = if (document.exists()) {
            document.data ?: emptyMap()
        } else {
            emptyMap()
        }
        map.entries.map { entry -> Trigger(snowflake, entry.key, entry.value as String) }
    }

    override suspend fun addTriggerForGuild(snowflake: Snowflake, trigger: String, response: String) {
        withContext(context) {
            val update = mapOf(trigger to response)
            _collection
                .document(snowflake.asString())
                .set(update, SetOptions.merge())
                .suspend()
        }
    }
}