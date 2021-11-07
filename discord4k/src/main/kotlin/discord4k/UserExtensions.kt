import discord4j.common.util.Snowflake
import discord4j.core.`object`.entity.User

suspend fun User.suspendAsMember(snowflake: Snowflake) = this.asMember(snowflake).suspend()