package com.kyledahlin.rulebot

import discord4j.common.util.Snowflake
import discord4j.core.DiscordClient
import discord4j.discordjson.json.ImmutableMessageCreateRequest
import discord4k.createMessageInChannel
import discord4k.suspendApplicationId
import discord4k.suspendUserInGuild

interface RulebotContext {
    suspend fun getUsernameInGuild(userId: Snowflake, guildId: Snowflake): String
    suspend fun sendMessageToChannel(snowflake: Snowflake, onBuild: ImmutableMessageCreateRequest.Builder.() -> Unit)
    suspend fun botId(): Snowflake
}

class RulebotContextImpl(private val client: DiscordClient) : RulebotContext {

    override suspend fun botId() = Snowflake.of(client.suspendApplicationId())

    override suspend fun getUsernameInGuild(userId: Snowflake, guildId: Snowflake): String {
        return client.suspendUserInGuild(userId, guildId).username()
    }

    override suspend fun sendMessageToChannel(
        snowflake: Snowflake,
        onBuild: ImmutableMessageCreateRequest.Builder.() -> Unit
    ) {
        client.createMessageInChannel(snowflake, onBuild)
    }
}