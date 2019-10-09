import discord4j.core.`object`.entity.Member

suspend fun Member.suspendVoiceState() = voiceState.suspend()