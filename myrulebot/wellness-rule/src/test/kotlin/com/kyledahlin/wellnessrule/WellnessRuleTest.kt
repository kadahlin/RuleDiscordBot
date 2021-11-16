package com.kyledahlin.wellnessrule

import com.kyledahlin.testutils.RuleBaseTest
import com.kyledahlin.testutils.builders.isNamed
import com.kyledahlin.testutils.builders.isSlashCommand
import com.kyledahlin.testutils.testGuildCreation
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import strikt.api.expectThat

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
}