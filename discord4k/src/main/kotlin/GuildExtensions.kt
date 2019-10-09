import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.Member
import kotlinx.coroutines.reactive.awaitFirstOrNull

suspend fun Guild.suspendOwner(): Member? = this.owner.suspend()