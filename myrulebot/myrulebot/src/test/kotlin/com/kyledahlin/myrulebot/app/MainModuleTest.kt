package com.kyledahlin.myrulebot.app

import com.kyledahlin.myrulebot.TestResponse
import com.kyledahlin.myrulebot.TestRule
import com.kyledahlin.rulebot.Response
import com.kyledahlin.rulebot.RuleBot
import com.kyledahlin.rulebot.bot.LogLevel
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import strikt.api.Assertion
import strikt.api.expectThat
import strikt.assertions.isEqualTo

val testModule = SerializersModule {
    polymorphic(Any::class) {
        subclass(TestResponse::class)
    }
}

val testJson = Json {
    serializersModule = basicResponseModules + testModule
}

class MainModuleTest {

    private lateinit var testRule: TestRule


    @BeforeEach
    fun setup() {
        testRule = TestRule()
    }

    @Test
    fun `test rule endpoints`() {
        withTestApplication({
            mainModule(
                LocalAnalytics(),
                RuleBot.make("", LogLevel.DEBUG, LocalAnalytics(), listOf(testRule), emptySet()),
                json = testJson
            )
        }) {
            handleRequest(HttpMethod.Get, "/rules").apply {
                expectThat(response)
                    .wasSuccess(GetRulesResponse(listOf("testRule")))
            }

            handleRequest(HttpMethod.Post, "/rules/testRule") {
                setBody(testJson.encodeToString(mapOf("data" to "something")))
            }.apply {
                expectThat(response)
                    .wasSuccess(TestResponse("configured"))
                expectThat(testRule.data).isEqualTo("""{"data":"something"}""")
            }
        }
    }
}

infix fun Assertion.Builder<TestApplicationResponse>.wasSuccess(withData: Any?): Assertion.Builder<TestApplicationResponse> =
    assert("data is equal to $withData") {
        if (it.status() != HttpStatusCode.OK) {
            fail(actual = it.status(), description = "was not a 200")
        }
        when (val response: Response = testJson.decodeFromString(it.content!!)) {
            is Response.Success -> when (response.data) {
                withData -> pass()
                else -> fail(actual = response.data)
            }
            is Response.Failure -> fail(actual = response, description = "was failure")
        }
    }

