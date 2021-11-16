package com.kyledahlin.wellnessrule

import com.google.cloud.firestore.Firestore
import com.kyledahlin.myrulebot.bot.suspend
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
        return doc.data?.get("id")?.let {
            (it as String).sf()
        }
    }

    suspend fun saveChannelIdForGuild(channelId: Snowflake, guildId: Snowflake) {
        _collection
            .document(guildId.asString())
            .set(mapOf("id" to channelId.asString()))
            .suspend()
    }

    suspend fun deleteChannelIdForGuild(guildId: Snowflake) {
        _collection
            .document(guildId.asString())
            .set(mapOf())
            .suspend()
    }
}