package com.kyledahlin.rulebot

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test

class ResponseSanityTest {
    @Test
    fun `response should be serializable back and forth`() {
        val response = Response(data = listOf("one", "two"))
        val encoded = Json.encodeToString(response)
        println(encoded)
    }
}