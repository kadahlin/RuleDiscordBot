import discord4j.core.`object`.entity.User
import discord4j.core.`object`.util.Snowflake

suspend fun User.suspendAsMember(snowflake: Snowflake) = this.asMember(snowflake).suspend()