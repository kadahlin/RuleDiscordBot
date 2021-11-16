package discord4k

import discord4j.core.event.domain.message.MessageCreateEvent

suspend fun MessageCreateEvent.suspendGuild() = guild.suspend()