/*
*Copyright 2021 Kyle Dahlin
*
*Licensed under the Apache License, Version 2.0 (the "License");
*you may not use this file except in compliance with the License.
*You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing, software
*distributed under the License is distributed on an "AS IS" BASIS,
*WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*See the License for the specific language governing permissions and
*limitations under the License.
*/
package com.kyledahlin.myrulebot.bot.reaction

import com.kyledahlin.myrulebot.assertLeft
import com.kyledahlin.myrulebot.assertRight
import com.kyledahlin.myrulebot.bot.RuleBaseTest
import com.kyledahlin.myrulebot.guildEmoji
import com.kyledahlin.rulebot.EventWrapper
import com.kyledahlin.rulebot.GuildWrapper
import com.kyledahlin.rulebot.NameSnowflake
import com.kyledahlin.rulebot.bot.MessageCreated
import com.kyledahlin.rulebot.sf
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ReactionRuleTest : RuleBaseTest() {
    private val _storage: ReactionStorage = mock {
        onBlocking { getReactionsForMember(any(), any()) }.thenReturn(emptyList())
    }
    private lateinit var _rule: ReactionRule
    val guildId = "2".sf()
    val event = MessageCreated()
    val eventWrapper: EventWrapper = mock {
        onBlocking { getGuildId() }.thenReturn(guildId)
    }
    val guildWrapper: GuildWrapper = mock()
    val emoji = guildEmoji()

    @BeforeEach
    override fun setup() {
        super.setup()
        _rule = ReactionRule(wrapper, cache, _storage, analytics)
        addEvent(event, eventWrapper)
        addGuildWrapper(guildId, guildWrapper)
    }

    @Test
    fun `Correct error on something that is not a valid command`() = runBlocking {
        _rule.configure("wrong").assertLeft(ReactionException.DataFormatException("wrong"))
    }

    @Test
    fun `No reactions are added if the members has no stored emojis`() = runBlocking {
        whenever(guildWrapper.getGuildEmojiForId(guildId)).thenReturn(guildEmoji())
        _rule.handleEvent(event)
        verify(eventWrapper, never()).addEmoji(any())
    }

    @Test
    fun `Reactions are added if the members has stored emojis`() = runBlocking {
        whenever(guildWrapper.getGuildEmojiForId(emoji.id)).thenReturn(emoji)
        whenever(_storage.getReactionsForMember(event.author, guildId)).thenReturn(listOf(emoji.id))
        _rule.handleEvent(event)
        verify(eventWrapper).addEmoji(emoji)
    }

    @Test
    fun `Correct exception is returned if an unknown command is passed`() = runBlocking {
        val command = ReactionRule.Command(guildId.asString(), action = "wrong")
        val result = _rule.configure(Json.encodeToJsonElement(command))
        result.assertLeft(ReactionException.UnknownConfigAction("wrong"))
    }

    @Test
    fun `Adding a reaction should hit the database`() = runBlocking {
        val command = ReactionRule.Command(guildId.asString(), action = "add", member = "1", emoji = "1")
        val result = _rule.configure(Json.encodeToJsonElement(command))
        verify(_storage).storeReactionForMember("1".sf(), guildId, "1".sf())
        result.assertRight(Unit)
    }

    @Test
    fun `Adding a reaction should return the correct error on storage issue`() = runBlocking {
        val command = ReactionRule.Command(guildId.asString(), action = "add", member = "1", emoji = "1")
        whenever(_storage.storeReactionForMember(any(), any(), any())).thenThrow(RuntimeException("something"))
        val result = _rule.configure(Json.encodeToJsonElement(command))
        result.assertLeft(ReactionException.StorageException("something"))
    }

    @Test
    fun `Removing a reaction should hit the database`() = runBlocking {
        val command = ReactionRule.Command(guildId.asString(), action = "remove", member = "1", emoji = "1")
        val result = _rule.configure(Json.encodeToJsonElement(command))
        verify(_storage).removeReactionForMember("1".sf(), guildId, "1".sf())
        result.assertRight(Unit)
    }

    @Test
    fun `Removing a reaction should return the correct error on storage issue`() = runBlocking {
        val command = ReactionRule.Command(guildId.asString(), action = "remove", member = "1", emoji = "1")
        whenever(_storage.removeReactionForMember(any(), any(), any())).thenThrow(RuntimeException("something"))
        val result = _rule.configure(Json.encodeToJsonElement(command))
        result.assertLeft(ReactionException.StorageException("something"))
    }

    @Test
    fun `Removing a reaction should return the correct error on format issue`() = runBlocking {
        val command = ReactionRule.Command(guildId.asString(), action = "remove", member = null, emoji = "1")
        val result = _rule.configure(Json.encodeToJsonElement(command))
        result.assertLeft(ReactionException.CommandMissingData(command))
    }

    @Test
    fun `Listing should return the correct discord error if emojis cant be loaded`() = runBlocking {
        val command = ReactionRule.Command(guildId.asString(), action = "list", member = null, emoji = null)
        whenever(_storage.getStoredReactions(guildId)).thenReturn(emptyList())
        whenever(guildWrapper.getEmojiNameSnowflakes()).thenThrow(java.lang.RuntimeException("something"))
        _rule.configure(Json.encodeToJsonElement(command)).assertLeft(ReactionException.DiscordException("something"))
    }

    @Test
    fun `getting emoji data should return the correct list`() = runBlocking {
        val command = ReactionRule.Command(guildId.asString(), action = "list", member = null, emoji = null)
        whenever(_storage.getStoredReactions(guildId)).thenReturn(emptyList())
        var result = _rule.configure(Json.encodeToJsonElement(command))
        result.assertRight(GuildInfo(emptyList(), emptyList()))

        whenever(guildWrapper.getEmojiNameSnowflakes()).thenReturn(listOf(NameSnowflake("first", "2".sf())))
        result = _rule.configure(Json.encodeToJsonElement(command))
        result.assertRight(GuildInfo(listOf(Emoji("first", "2")), emptyList()))

        val stored = StoredReaction("1".sf(), guildId, "2".sf())
        whenever(_storage.getStoredReactions(guildId)).thenReturn(listOf(stored))
        result = _rule.configure(Json.encodeToJsonElement(command))
        result.assertRight(
            GuildInfo(
                listOf(Emoji("first", "2")),
                listOf(AddedEmoji("first", stored.member.asString(), stored.emoji.asString()))
            )
        )
    }
}