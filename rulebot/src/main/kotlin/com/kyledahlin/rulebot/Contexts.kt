package com.kyledahlin.rulebot

import discord4j.common.util.Snowflake
import discord4j.core.DiscordClient
import discord4j.core.`object`.entity.User
import discord4j.core.event.domain.guild.GuildCreateEvent
import discord4j.core.event.domain.interaction.ButtonInteractionEvent
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.event.domain.interaction.UserInteractionEvent
import discord4j.discordjson.json.ApplicationCommandRequest
import discord4k.builders.InteractionApplicationCommandCallbackSpecKt
import discord4k.builders.InteractionReplyEditSpecKt
import discord4k.builders.MessageCreateSpecKt
import discord4k.interactions.*
import discord4k.suspendApplicationId
import discord4k.suspendCreateApplicationCommand
import discord4k.suspendPrivateChannel
import suspendCreateMessage

interface GuildCreateContext {
    val guildId: Snowflake
    suspend fun registerApplicationCommand(request: ApplicationCommandRequest)
}

internal class GuildCreateContextImpl(private val client: DiscordClient, private val event: GuildCreateEvent) :
    GuildCreateContext {

    override val guildId: Snowflake
        get() = event.guild.id

    override suspend fun registerApplicationCommand(request: ApplicationCommandRequest) {
        client
            .applicationService
            .suspendCreateApplicationCommand(client.suspendApplicationId(), event.guild.id.asLong(), request)
    }
}

interface ChatInputInteractionContext {
    val name: String
    val channelId: Snowflake
    val guildId: Snowflake
    val userId: Snowflake
    suspend fun getOption(name: String): String?
    suspend fun reply(spec: InteractionApplicationCommandCallbackSpecKt.() -> Unit)
    suspend fun editReply(spec: InteractionReplyEditSpecKt.() -> Unit)
    suspend fun deferReply(withEphemeral: Boolean)
    suspend fun sendMessageToUser(spec: MessageCreateSpecKt.() -> Unit)
}

internal class ChatInputInteractionContextImpl(private val event: ChatInputInteractionEvent) :
    ChatInputInteractionContext {

    override val channelId: Snowflake
        get() = event.interaction.channelId

    override val userId: Snowflake
        get() = event.interaction.user.id

    override val guildId: Snowflake
        get() = event.interaction.guild.block()!!.id

    override val name: String
        get() = event.commandName

    override suspend fun reply(spec: InteractionApplicationCommandCallbackSpecKt.() -> Unit) {
        event.suspendReply(spec)
    }

    override suspend fun deferReply(withEphemeral: Boolean) {
        event.suspendDeferReply(withEphemeral)
    }

    override suspend fun editReply(spec: InteractionReplyEditSpecKt.() -> Unit) {
        event.suspendEditReply(spec)
    }

    override suspend fun sendMessageToUser(spec: MessageCreateSpecKt.() -> Unit) {
        event.interaction.user
            .suspendPrivateChannel()
            .suspendCreateMessage(spec)
    }

    override suspend fun getOption(name: String): String? =
        event.getOption(name)
            .flatMap { it.value }
            .map { it.asString() }
            .orElse(null)
}

interface ButtonInteractionEventContext {
    val channelId: Snowflake
    val customId: String
    suspend fun reply(spec: InteractionApplicationCommandCallbackSpecKt.() -> Unit)
    suspend fun editReply(spec: InteractionReplyEditSpecKt.() -> Unit)
    suspend fun edit(spec: InteractionApplicationCommandCallbackSpecKt.() -> Unit)
}

internal class ButtonInteractionEventContextImpl(private val event: ButtonInteractionEvent) :
    ButtonInteractionEventContext {

    override val channelId: Snowflake
        get() = event.interaction.channelId

    override val customId: String
        get() = event.customId

    override suspend fun reply(spec: InteractionApplicationCommandCallbackSpecKt.() -> Unit) {
        event.suspendReply(spec)
    }

    override suspend fun editReply(spec: InteractionReplyEditSpecKt.() -> Unit) {
        event.suspendEditReply(spec)
    }

    override suspend fun edit(spec: InteractionApplicationCommandCallbackSpecKt.() -> Unit) {
        event.suspendEdit(spec)
    }
}

interface UserInteractionContext {
    val user: Pair<Snowflake, String>
    val targetUser: Pair<Snowflake, String>
    val guildId: Snowflake
    val channelId: Snowflake
    suspend fun reply(spec: InteractionApplicationCommandCallbackSpecKt.() -> Unit)
    suspend fun sendMessageToTargetUser(spec: MessageCreateSpecKt.() -> Unit)
    suspend fun sendMessageToInitialUser(spec: MessageCreateSpecKt.() -> Unit)
}

internal class UserInteractionContextImpl(private val event: UserInteractionEvent) :
    UserInteractionContext {

    override val channelId: Snowflake
        get() = event.interaction.channelId

    override val guildId: Snowflake
        get() = event.interaction.guildId.get()

    override val user: Pair<Snowflake, String>
        get() = idToUsername(event.interaction.user)

    override val targetUser: Pair<Snowflake, String>
        get() = idToUsername(event.targetUser.block()!!)

    private fun idToUsername(user: User): Pair<Snowflake, String> {
        val id = user.id
        val username = user.asMember(guildId).block()!!.username
        return id to username
    }

    override suspend fun reply(spec: InteractionApplicationCommandCallbackSpecKt.() -> Unit) {
        event.suspendReply(spec)
    }

    override suspend fun sendMessageToTargetUser(spec: MessageCreateSpecKt.() -> Unit) {
        event
            .suspendTargetUser()
            .suspendPrivateChannel()
            .suspendCreateMessage(spec)
    }

    override suspend fun sendMessageToInitialUser(spec: MessageCreateSpecKt.() -> Unit) {
        event.interaction.user
            .suspendPrivateChannel()
            .suspendCreateMessage(spec)
    }
}