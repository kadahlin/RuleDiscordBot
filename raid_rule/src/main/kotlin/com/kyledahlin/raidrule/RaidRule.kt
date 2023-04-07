/*
*Copyright 2020 Kyle Dahlin
*
*Licensed under the Apache License, Version 2.0 (the "License");
*you may not use this file except in compliance with the License.
*You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing, software
*distributed under the License is distributed on an "AS IS" BASIS,
*WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*See the License for the specific language governing permissions and
*limitations under the License.
*/
package com.kyledahlin.raidrule

import com.kyledahlin.rulebot.ButtonInteractionEventContext
import com.kyledahlin.rulebot.ChatInputInteractionContext
import com.kyledahlin.rulebot.GuildCreateContext
import com.kyledahlin.rulebot.bot.Logger.logDebug
import com.kyledahlin.rulebot.bot.Rule
import discord4j.core.`object`.component.ActionRow
import discord4j.core.`object`.component.Button
import discord4j.discordjson.json.ApplicationCommandRequest
import javax.inject.Inject

private const val RAIDRULE = "raidrule"
private const val ROLE_COMMAND = "role-check"

class RaidRule @Inject constructor(
) :
    Rule(RAIDRULE) {

    override val priority: Priority
        get() = Priority.NORMAL

    override fun handlesCommand(name: String): Boolean {
        return name == ROLE_COMMAND
    }

    override suspend fun onGuildCreate(context: GuildCreateContext) {
        super.onGuildCreate(context)

        context.registerApplicationCommand(
            ApplicationCommandRequest.builder()
                .name(ROLE_COMMAND)
                .description("Create role assignments for this activity")
                .build()
        )
    }

    override suspend fun onSlashCommand(context: ChatInputInteractionContext) {
        if (context.name == ROLE_COMMAND) {
            context.reply {
                withEphemeral()
                addComponent {
                    ActionRow.of(
                        activityTypes.map { Button.primary(it.id, it.name) }
                    )
                }
            }
        }
    }

    override suspend fun onButtonEvent(context: ButtonInteractionEventContext) {
        logDebug { "on button for ${context.customId}" }
        // Match a category type
        activityTypes.firstOrNull { it.id == context.customId }
            ?.let { type -> context.replyWithActivities(type.activities) }

        // match a specific activity
        activityTypes.flatMap { it.activities }
            .filterIsInstance<DestinyEncounterActivity>()
            .firstOrNull { it.id == context.customId }
            ?.let { activity -> context.replyWithActivities(activity.encounters) }

        // match a specific encounter
        activityTypes.flatMap { it.activities }
            .filterIsInstance<DestinyEncounterActivity>()
            .flatMap { it.encounters }
            .firstOrNull { it.id == context.customId }?.let { encounter ->
                val user = context.user
                val guild = context.guild()
                val voiceState = user.asMember(context.guild().id).block()?.voiceState?.block()
                if (voiceState == null) {
                    logDebug { "not in voice when using command" }
                    context.reply {
                        withEphemeral()
                        content { "You need to be in voice to use this" }
                    }
                    return
                }
                val voiceUsers =
                    this@RaidRule.context.getUsernamesInVoice(
                        guild,
                        voiceState
                    )
                logDebug { "${voiceUsers.size} users in voice" }
                context.reply {
                    content { encounter.assignRoles(voiceUsers.shuffled()) }
                }
            }
    }

    private suspend fun ButtonInteractionEventContext.replyWithActivities(list: List<DestinyActivity>) {
        reply {
            withEphemeral()
            list.chunked(5)
                .map { sublist ->
                    addComponent {
                        ActionRow.of(
                            sublist.map { Button.primary(it.id, it.name) }
                        )
                    }
                }
        }
    }
}

val activityTypes = listOf(
    ActivityType("Raids", id = "raids", (SmallBoyRaids + BigBoyRaids).sortedBy { it.name })
)