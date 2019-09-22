import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.Member

suspend fun Guild.suspendOwner(): Member = this.owner.suspend()