package discord4k

import discord4j.core.`object`.VoiceState

suspend fun VoiceState.suspendChannel() = channel.suspend()

suspend fun VoiceState.suspendMember() = member.suspend()