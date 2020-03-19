import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.runBlocking
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

internal suspend inline fun <T> Mono<T>.suspend(): T? = awaitFirstOrNull()

internal suspend inline fun <T> Flux<T>.toList(): List<T> = this.collectList().awaitFirst()

@ExperimentalCoroutinesApi
fun <T> Flux<T>.fluxToChannel(channel: SendChannel<T>) {
    this.subscribe {
        println("got flux $it")
        runBlocking { channel.send(it) }
    }
}

