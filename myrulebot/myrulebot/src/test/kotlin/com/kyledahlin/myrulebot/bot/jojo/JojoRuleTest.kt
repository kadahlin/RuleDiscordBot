package com.kyledahlin.myrulebot.bot.jojo

import com.kyledahlin.testutils.TestChatInputInteractionContext
import com.kyledahlin.testutils.RuleBaseTest
import com.kyledahlin.myrulebot.bot.jojorule.*
import com.kyledahlin.testutils.builders.hasContent
import com.kyledahlin.testutils.builders.hasEmbeddedImage
import com.kyledahlin.testutils.builders.isNamed
import com.kyledahlin.testutils.builders.isSlashCommand
import com.kyledahlin.testutils.testGuildCreation
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isTrue

class JojoRuleTest : RuleBaseTest() {

    @MockK
    private lateinit var _api: JojoApi

    @MockK(relaxUnitFun = true)
    private lateinit var _storage: JojoMemeStorage

    private lateinit var _jojo: JojoMemeRule

    override fun init() {
        coEvery { _api.getPosts() } returns listOf(RedditChild("kind", RedditChildData("title", false, "url", "id")))
        _jojo = JojoMemeRule(_storage, _api, analytics)
    }

    @Test
    fun `Jojo registers its command`() = runBlocking<Unit> {
        expectThat(testGuildCreation(_jojo).first())
            .isNamed("jojo")
            .isSlashCommand()
    }

    @Test
    fun `Jojo sends the correct response`() = runBlocking<Unit> {
        val context = TestChatInputInteractionContext()
        _jojo.onSlashCommand(context)
        expectThat(context.wasDeferred).isTrue()
        expectThat(context.edits.first())
            .hasContent("title")
            .hasEmbeddedImage("url")
        expectThat(context.replies.isEmpty())
    }
}