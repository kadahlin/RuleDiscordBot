import discord4j.core.`object`.VoiceState

suspend fun VoiceState.suspendChannel() = channel.suspend()