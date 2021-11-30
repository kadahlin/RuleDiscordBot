package discord4k

import discord4j.common.util.Snowflake
import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.GuildEmoji
import discord4j.core.`object`.entity.Member

suspend fun Guild.suspendOwner(): Member? = this.owner.suspend()

suspend fun Guild.suspendEmojis(): List<GuildEmoji> = this.emojis.toList()

suspend fun Guild.suspendMembers(): List<Member> = this.members.toList()

suspend fun Guild.suspendGetGuildEmojiById(id: Snowflake) = this.getGuildEmojiById(id).suspend()