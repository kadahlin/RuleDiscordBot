import discord4j.common.util.Snowflake
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.channel.MessageChannel
import discord4j.core.spec.MessageCreateSpec

suspend fun MessageChannel.suspendGetMessageById(snowflake: Snowflake): Message? =
    this.getMessageById(snowflake).suspend()

suspend fun MessageChannel.suspendCreateMessage(content: String): Message? = this.createMessage(content).suspend()

suspend fun MessageChannel.suspendCreateMessage(withSpec: MessageCreateSpec.Builder.() -> Unit): Message? =
    this.createMessage(MessageCreateSpec.builder().apply(withSpec).build()).suspend()