package com.kyledahlin.wellnessrule

import arrow.core.Either
import com.kyledahlin.testutils.RuleBaseTest
import com.kyledahlin.testutils.builders.isNamed
import com.kyledahlin.testutils.builders.isSlashCommand
import com.kyledahlin.testutils.testGuildCreation
import com.kyledahlin.wellness.models.WellnessResponse
import com.kyledahlin.wellness.models.wellnessSerializerModule
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class WellnessRuleTest : RuleBaseTest() {

    @MockK(relaxUnitFun = true)
    private lateinit var _storage: WellnessStorage

    private lateinit var _wellness: WellnessRule

    override fun init() {
        _wellness = WellnessRule(analytics, _storage)
    }

    @Test
    fun `Wellness registers its command`() = runBlocking<Unit> {
        expectThat(testGuildCreation(_wellness).first())
            .isNamed("wellness-register")
            .isSlashCommand()
    }

    @Test
    fun `Calls are forwarded to the storage for enabling`() = runBlocking<Unit> {
        val data = json.encodeToString(mapOf("toEnable" to listOf("1"), "toDisable" to listOf("2", "3")))
        val response = _wellness.configure(data)
        expectThat((response as Either.Right).value).isEqualTo(WellnessResponse("successfully enabled for 1 and disabled for 2 guilds"))
        coVerify { _storage.enableForGuild(listOf("1")) }
        coVerify { _storage.disableForGuild(listOf("2", "3")) }
    }
}

private val json = Json {
    serializersModule = wellnessSerializerModule
}