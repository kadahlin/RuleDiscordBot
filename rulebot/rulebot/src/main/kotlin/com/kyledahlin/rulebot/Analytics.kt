package com.kyledahlin.rulebot

interface Analytics {
    suspend fun logLifecycle(name: String, data: String)
    suspend fun logRuleFailed(ruleName: String, reason: String)
}