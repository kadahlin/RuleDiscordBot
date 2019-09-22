import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.Member
import reactor.core.publisher.Mono
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

suspend fun Guild.suspendOwner(): Member = this.owner.suspend()