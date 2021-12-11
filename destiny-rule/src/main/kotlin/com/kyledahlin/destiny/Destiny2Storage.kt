package com.kyledahlin.destiny

import com.google.cloud.firestore.DocumentReference
import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.SetOptions
import com.kyledahlin.myrulebot.bot.suspend
import javax.inject.Inject

open class Destiny2Storage @Inject constructor(firestore: Firestore) {
    private val _collection = firestore.collection("destiny2")

    private val _coreDoc: DocumentReference
        get() = _collection
            .document("core")

    suspend fun getAccessAndRefreshToken(): Pair<String, String> {
        val doc = _coreDoc.get().suspend()
        return doc.data?.get("accessToken")!! as String to doc.data?.get("refreshToken")!! as String
    }

    suspend fun setRefreshAndAccessToken(accessToken: String, refreshToken: String) {
        _coreDoc
            .set(mapOf("accessToken" to accessToken, "refreshToken" to refreshToken), SetOptions.merge())
            .suspend()
    }

    suspend fun getClientId(): String = _coreDoc
        .get()
        .suspend()
        .get("clientId") as String

    suspend fun getApiKey(): String = _coreDoc
        .get()
        .suspend()
        .get("apiKey") as String

    suspend fun getClientSecret(): String = _coreDoc
        .get()
        .suspend()
        .get("clientSecret") as String
}