/*
*Copyright 2021 Kyle Dahlin
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
package com.kyledahlin.testutils

import com.kyledahlin.rulebot.ChatInputInteractionContext
import com.kyledahlin.rulebot.GuildCreateContext
import com.kyledahlin.rulebot.bot.Rule
import discord4j.common.util.Snowflake
import discord4j.discordjson.json.ApplicationCommandRequest
import discord4k.builders.InteractionApplicationCommandCallbackSpecKt
import discord4k.builders.InteractionReplyEditSpecKt
import discord4k.builders.MessageCreateSpecKt
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot

suspend fun testGuildCreation(rule: Rule): List<ApplicationCommandRequest> {
    val slot = slot<ApplicationCommandRequest>()
    val result = mutableListOf<ApplicationCommandRequest>()
    val context: GuildCreateContext = mockk {
        coEvery { registerApplicationCommand(request = capture(slot)) } answers {
            result.add(slot.captured)
        }
    }
    rule.onGuildCreate(context)
    return result
}

class TestChatInputInteractionContext :
    ChatInputInteractionContext {
    val replies = mutableListOf<InteractionApplicationCommandCallbackSpecKt>()
    val edits = mutableListOf<InteractionReplyEditSpecKt>()
    val options = mutableMapOf<String, String>()
    var userMessageSpec: MessageCreateSpecKt? = null

    var wasDeferred: Boolean = false
    var wasDeferredEphmererally: Boolean = false

    override val name: String
        get() = "name"

    override val channelId: Snowflake
        get() = Snowflake.of("1")

    override val guildId: Snowflake
        get() = Snowflake.of("1")

    override val userId: Snowflake
        get() = Snowflake.of("1")

    override suspend fun getOption(name: String): String? {
        return options["name"]
    }

    override suspend fun reply(spec: InteractionApplicationCommandCallbackSpecKt.() -> Unit) {
        replies.add(InteractionApplicationCommandCallbackSpecKt().apply(spec))
    }

    override suspend fun deferReply(withEphemeral: Boolean) {
        wasDeferred = true
        wasDeferredEphmererally = withEphemeral
    }

    override suspend fun editReply(spec: InteractionReplyEditSpecKt.() -> Unit) {
        edits.add(InteractionReplyEditSpecKt().apply(spec))
    }

    override suspend fun sendMessageToUser(spec: MessageCreateSpecKt.() -> Unit) {
        userMessageSpec = MessageCreateSpecKt().apply(spec)
    }
}
