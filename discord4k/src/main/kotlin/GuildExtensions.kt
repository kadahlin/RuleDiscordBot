import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.GuildEmoji
import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.util.Snowflake
import kotlinx.coroutines.reactive.awaitFirstOrNull

suspend fun Guild.suspendOwner(): Member? = this.owner.suspend()

suspend fun Guild.suspendEmojis(): List<GuildEmoji> = this.emojis.toList()

suspend fun Guild.suspendGetGuildEmojiById(id: Snowflake) = this.getGuildEmojiById(id).suspend()