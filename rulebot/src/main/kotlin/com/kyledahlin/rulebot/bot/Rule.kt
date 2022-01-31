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
package com.kyledahlin.rulebot.bot

import arrow.core.Either
import arrow.core.right
import com.kyledahlin.rulebot.*
import com.kyledahlin.rulebot.bot.Logger.logInfo
import discord4j.common.util.Snowflake
import discord4j.core.DiscordClient
import discord4j.core.`object`.entity.Message
import discord4j.core.event.domain.VoiceStateUpdateEvent
import discord4k.suspendGuild
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*

/**
 * A self-contained piece of logic that operates on the messages given to it.
 */
abstract class Rule(
    val ruleName: String,
) {

    protected lateinit var context: RulebotContext

    internal fun setContext(client: DiscordClient) {
        context = RulebotContextImpl(client = client)
    }

    enum class Priority {
        HIGH, NORMAL, LOW
    }

    abstract fun handlesCommand(name: String): Boolean

    /**
     * Process this event from *anywhere* and determine if action is necessary, or return information based on this request
     */
    open suspend fun configure(data: Any): Either<Any, Any> = emptyMap<String, String>().right()

    //@OverridingMethodsMustInvokeSuper
    open suspend fun onGuildCreate(context: GuildCreateContext) {
        logInfo { "Attaching commands" }
    }

    open suspend fun onSlashCommand(context: ChatInputInteractionContext) {}

    open suspend fun onUserCommand(context: UserInteractionContext) {}

    open suspend fun onButtonEvent(context: ButtonInteractionEventContext) {}

    open suspend fun onVoiceUpdate(event: VoiceStateUpdateEvent) {}

    open val priority: Priority = Priority.NORMAL

    protected open fun isAdminOnly() = true

    override fun equals(other: Any?): Boolean {
        if (other is Rule) {
            return other.ruleName == ruleName
        }
        return false
    }

    protected suspend fun measureExecutionTime(name: String, block: suspend () -> Unit) {
        val startTime = System.currentTimeMillis()
        block()
        val totalTime = System.currentTimeMillis() - startTime
        logInfo { "($name) took $totalTime milliseconds" }
    }
}

data class RoleSnowflake(
    val snowflake: Snowflake,
    val guildSnowflake: Snowflake?,
    val isRole: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (other is RoleSnowflake) {
            return other.snowflake == snowflake
        }
        return false
    }
}

//return all users and roles that were mentioned in this message
internal suspend fun Message.getSnowflakes(): Set<RoleSnowflake> {
    val guildId = suspendGuild()?.id
    val users = userMentionIds.map { RoleSnowflake(it, guildId) }
    val roles = roleMentionIds.map { RoleSnowflake(it, guildId, isRole = true) }
    return users.union(roles)
}

val client by lazy {
    HttpClient(Apache) {
        install(JsonFeature) {
            serializer = KotlinxSerializer(kotlinx.serialization.json.Json { ignoreUnknownKeys = true })
        }
    }
}