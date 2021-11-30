package com.kyledahlin.myrulebot.bot.jojorule

import com.google.cloud.firestore.FieldValue
import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.SetOptions
import com.kyledahlin.myrulebot.bot.suspend
import discord4j.common.util.Snowflake
import javax.inject.Inject

private const val IDS = "ids"

open class JojoMemeStorage @Inject constructor(
    firestore: Firestore
) {

    val _collection = firestore.collection("jojomeme")

    suspend fun getIdsForServer(guildId: Snowflake): Set<String> {
        val doc = _collection
            .document(guildId.asString())
            .get()
            .suspend()
        return doc.data?.get(IDS)?.let { ids ->
            (ids as List<String>).toSet()
        } ?: emptySet()
    }

    suspend fun saveIdToGuild(id: String, guildId: Snowflake) {
        _collection
            .document(guildId.asString())
            .set(mapOf(IDS to FieldValue.arrayUnion(id)), SetOptions.merge())
            .suspend()
    }
}