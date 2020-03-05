package com.kyledahlin.rulebot

import com.kyledahlin.rulebot.bot.MessageCreated
import com.nhaarman.mockitokotlin2.mock
import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.util.Snowflake
import org.junit.jupiter.api.Test

class DiscordCacheTest {
    @Test
    fun `read events from the cache`() {
        val cache = DiscordCache()
        val event = MessageCreated(Snowflake.of(1), "", Snowflake.of(1), emptySet(), emptySet())
        val eventTwo = MessageCreated(Snowflake.of(1), "", Snowflake.of(1), emptySet(), emptySet())
//
//        cache.add(event, mock(), mock(), mock())
//
//        assert(cache.getMetadataForEvent(event) != null)
//        assert(cache.getMetadataForEvent(eventTwo) != cache.getMetadataForEvent(event))
    }
}