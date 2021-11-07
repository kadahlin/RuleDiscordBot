package com.kyledahlin.myrulebot.bot.jojo

import com.kyledahlin.myrulebot.TestChatInputInteractionContext
import com.kyledahlin.myrulebot.bot.RuleBaseTest
import com.kyledahlin.myrulebot.bot.jojorule.*
import com.kyledahlin.myrulebot.builders.hasContent
import com.kyledahlin.myrulebot.builders.hasEmbeddedImage
import com.kyledahlin.myrulebot.builders.isNamed
import com.kyledahlin.myrulebot.builders.isSlashCommand
import com.kyledahlin.myrulebot.testGuildCreation
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import strikt.api.expectThat

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
        expectThat(context.replies.first())
            .hasContent("title")
            .hasEmbeddedImage("url")
    }
}