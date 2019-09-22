import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.runBlocking
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

internal suspend inline fun <T> Mono<T>.suspend(): T = suspendCoroutine { cont ->
    this.subscribe({
        cont.resume(it)
    }, {
        cont.resumeWithException(it)
    })
}

internal suspend inline fun <T> Flux<T>.toList(): List<T> = this.collectList().suspend()

@ExperimentalCoroutinesApi
fun <T> Flux<T>.fluxToChannel(channel: SendChannel<T>) {
    this.subscribe {
        println("got flux $it")
        runBlocking { channel.send(it) }
    }
}

