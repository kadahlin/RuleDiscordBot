package com.kyledahlin.rulebot

import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.GuildEmoji
import discord4j.core.`object`.util.Snowflake
import discord4j.core.spec.MessageCreateSpec
import suspendEmojis
import suspendGetGuildEmojiById
import suspendMembers

typealias NameSnowflake = Pair<String, Snowflake>

interface GuildWrapper {

    val id: Snowflake

    suspend fun sendMessage(message: String)

    suspend fun sendMessage(withSpec: MessageCreateSpec.() -> Unit)

    suspend fun getMemberNameSnowflakes(): List<NameSnowflake>

    suspend fun getEmojiNameSnowflakes(): List<NameSnowflake>

    suspend fun getGuildEmojiForId(id: Snowflake): GuildEmoji?
}

class GuildWrapperImpl(private val guild: Guild) : GuildWrapper {

    override val id: Snowflake
        get() = guild.id

    override suspend fun sendMessage(message: String) {

    }

    override suspend fun sendMessage(withSpec: MessageCreateSpec.() -> Unit) {

    }

    override suspend fun getEmojiNameSnowflakes(): List<NameSnowflake> {
        return guild.suspendEmojis().map { it.name to it.id }
    }

    override suspend fun getMemberNameSnowflakes(): List<NameSnowflake> {
        return guild.suspendMembers().map { it.displayName to it.id }
    }

    override suspend fun getGuildEmojiForId(id: Snowflake): GuildEmoji? {
        return guild.suspendGetGuildEmojiById(id)
    }
}