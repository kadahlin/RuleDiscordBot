package com.kyledahlin.wellnessrule

import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.SetOptions
import com.kyledahlin.myrulebot.bot.suspend
import com.kyledahlin.rulebot.bot.Logger.logDebug
import com.kyledahlin.rulebot.sf
import discord4j.common.util.Snowflake
import javax.inject.Inject

class WellnessStorage @Inject constructor(
    firestore: Firestore
) {
    private val _collection = firestore.collection("wellness")

    suspend fun getChannelIdForGuild(guildId: Snowflake): Snowflake? {
        val doc = _collection
            .document(guildId.asString())
            .get()
            .suspend()
        doc.data?.get("disabled")?.let {
            if (it as? Boolean == true) {
                logDebug { "disabled for this guild, returning null channel" }
                return null
            }
        }
        return doc.data?.get("id")?.let {
            (it as String).sf()
        }
    }

    suspend fun saveChannelIdForGuild(channelId: Snowflake, guildId: Snowflake) {
        _collection
            .document(guildId.asString())
            .set(mapOf("id" to channelId.asString()), SetOptions.merge())
            .suspend()
    }

    suspend fun deleteChannelIdForGuild(guildId: Snowflake) {
        _collection
            .document(guildId.asString())
            .set(mapOf("id" to ""), SetOptions.merge())
            .suspend()
    }

    suspend fun disableForGuild(guildIds: List<String>) {
        guildIds.forEach {
            _collection
                .document(it)
                .set(mapOf("disabled" to true), SetOptions.merge())
                .suspend()
        }

    }

    suspend fun enableForGuild(guildIds: List<String>) {
        guildIds.forEach {
            _collection
                .document(it)
                .set(mapOf("disabled" to false), SetOptions.merge())
                .suspend()
        }
    }
}