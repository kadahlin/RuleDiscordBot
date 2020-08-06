package com.kyledahlin.rulebot.analytics

import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

const val RULE_CONFIGURATION = "rule_configuration"

interface Analytics {
    suspend fun logLifecycle(name: String, data: String)

    suspend fun logRuleFailed(ruleName: String, reason: String)
}

fun createAnalytics(connectionString: String, databaseName: String): Analytics =
    AnalyticsImpl(connectionString, databaseName)

internal class AnalyticsImpl(url: String, databaseName: String) : Analytics {
    private val client = KMongo.createClient(connectionString = url).coroutine
    private val database = client.getDatabase(databaseName)
    private val ruleFailedCollection = database.getCollection<RuleFailed>()
    private val lifecycleCollection = database.getCollection<Lifecycle>()

    override suspend fun logLifecycle(name: String, details: String) {
        lifecycleCollection.insertOne(Lifecycle(name, details))
    }

    override suspend fun logRuleFailed(ruleName: String, reason: String) {
        ruleFailedCollection.insertOne(RuleFailed(ruleName, reason))
    }
}

data class RuleFailed(val ruleName: String, val reason: String, val timestamp: Long = System.currentTimeMillis())

data class Lifecycle(
    val lifecycleName: String,
    val details: String = "",
    val timestamp: Long = System.currentTimeMillis()
)