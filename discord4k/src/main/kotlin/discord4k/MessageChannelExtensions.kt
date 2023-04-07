package discord4k

import discord4j.common.util.Snowflake
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.channel.MessageChannel
import discord4k.builders.MessageCreateSpecKt
import discord4k.builders.messageCreateSpecKt

suspend fun MessageChannel.suspendGetMessageById(snowflake: Snowflake): Message? =
    this.getMessageById(snowflake).suspend()

suspend fun MessageChannel.suspendCreateMessage(content: String): Message? = this.createMessage(content).suspend()

suspend fun MessageChannel.suspendCreateMessage(spec: MessageCreateSpecKt.() -> Unit): Message? =
    this.createMessage(messageCreateSpecKt(spec)).suspend()