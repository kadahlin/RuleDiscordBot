package discord4k

import discord4j.core.event.EventDispatcher
import discord4j.core.event.domain.Event
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

val eventChannel = Channel<Event>()

@FlowPreview
@ExperimentalCoroutinesApi
inline fun <reified T : Event> EventDispatcher.on(): Flow<T> {
    on(T::class.java).fluxToChannel(eventChannel)
    return eventChannel.consumeAsFlow().filter { it is T }.map { it as T }
}
