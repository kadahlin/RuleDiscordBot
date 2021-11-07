package discord4k

import discord4j.common.util.Snowflake
import discord4j.core.`object`.entity.User
import discord4j.core.`object`.entity.channel.PrivateChannel

suspend fun User.suspendAsMember(snowflake: Snowflake) = this.asMember(snowflake).suspend()

suspend fun User.suspendPrivateChannel(): PrivateChannel = this.privateChannel.suspend()!!