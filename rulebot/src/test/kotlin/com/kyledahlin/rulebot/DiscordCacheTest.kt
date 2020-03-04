package com.kyledahlin.rulebot

import com.kyledahlin.rulebot.bot.MessageCreated
import discord4j.core.`object`.util.Snowflake
import org.junit.jupiter.api.Test

class DiscordCacheTest {
    @Test
    fun `read events from the cache`() {
        val cache = DiscordCache()
        val event = MessageCreated(Snowflake.of(1), "", Snowflake.of(1), emptySet(), emptySet())
//        cache.add(event, mock(), mock(), mock())
    }
}