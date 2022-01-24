package discord4k

import discord4j.core.`object`.VoiceState
import discord4j.core.`object`.entity.channel.VoiceChannel

suspend fun VoiceChannel.suspendVoiceStates(): List<VoiceState> = voiceStates.toList()