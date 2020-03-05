package com.kyledahlin.rulebot.bot.marxrule

import com.kyledahlin.rulebot.DiscordWrapper
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class MarxRuleTest {

    private lateinit var _wrapper: DiscordWrapper

    @BeforeEach
    fun setup() {
        _wrapper = mock()
    }

    @Test
    fun `marx rule should respond to the trigger`() = runBlocking {
        val rule = MarxPassageRule(mock()) { _wrapper }
        assert(rule.handleEvent(MarxPassageRule.getTestValidEvent()))
        verify(_wrapper).sendMessage(any<String>())
    }

    @Test
    fun `mark rule should not respond to an invalid message`() = runBlocking {
        val rule = MarxPassageRule(mock()) { _wrapper }
        assert(!rule.handleEvent(MarxPassageRule.getTestInvalidEvent()))
        verify(_wrapper, never()).sendMessage(any<String>())
    }
}