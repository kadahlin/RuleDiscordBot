import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.channel.MessageChannel
import discord4j.core.`object`.reaction.ReactionEmoji

suspend fun Message.suspendGuild(): Guild? = this.guild.suspend()

suspend fun Message.suspendChannel(): MessageChannel? = this.channel.suspend()

suspend fun Message.suspendAddReaction(emoji: ReactionEmoji) = this.addReaction(emoji).suspend()

suspend fun Message.suspendUserMentions() = this.userMentions.toList()

suspend fun Message.suspendDelete() = this.delete().suspend()