package com.kyledahlin.psycho

import com.kyledahlin.rulebot.bot.Logger.logDebug
import com.kyledahlin.rulebot.bot.Rule
import discord4j.common.util.Snowflake
import discord4j.core.`object`.VoiceState
import discord4j.core.event.domain.VoiceStateUpdateEvent
import discord4j.core.spec.VoiceChannelEditSpec
import discord4k.suspendChannel
import discord4k.suspendMember
import discord4k.suspendRoles
import discord4k.suspendVoiceStates
import javax.inject.Inject

private const val PSYCHO_ROLE_NAME = "Psychos"

class PsychoRule @Inject constructor() : Rule("Psychos") {
    override fun handlesCommand(name: String) = false

    private val originalNames = mutableMapOf<Snowflake, String>()

    override suspend fun onVoiceUpdate(event: VoiceStateUpdateEvent) {
        if (!event.isJoinEvent) {
            logDebug { "processing move or leave" }
            checkMembers(event.old.get())
        }
        if (!event.isLeaveEvent) {
            logDebug { "processing move or join" }
            checkMembers(event.current)
        }
    }

    private suspend fun checkMembers(state: VoiceState) {
        val channel = state.suspendChannel()!!
        val psychosPresent = channel
            .suspendVoiceStates()
            .map { it.suspendMember()!! }
            .filter { it.suspendRoles().any { role -> role.data.name() == PSYCHO_ROLE_NAME } }
            .size

        logDebug { "detected $psychosPresent psychos on this event" }

        var originalName = originalNames[channel.id]
        if (originalName == null) {
            originalNames[channel.id] = channel.name
            originalName = channel.name
        }
        if (psychosPresent > 0) {
            channel.edit(
                VoiceChannelEditSpec.builder()
                    .name("(${psychosPresent} psychos) $originalName")
                    .build()
            )
                .block()
            logDebug { "finished changing the name for detected" }
        } else {
            channel.edit(
                VoiceChannelEditSpec.create()
                    .withName(originalName!!)
            ).block()
            logDebug { "Reset name to original" }
        }
    }
}