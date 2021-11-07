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
import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper
import com.kyledahlin.rulebot.*
import com.kyledahlin.rulebot.bot.Logger.logInfo
import discord4j.common.util.Snowflake
import discord4j.core.DiscordClient
import discord4j.core.`object`.entity.Message
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import suspendGuild
import java.io.File

/**
 * A self contained piece of logic that operates on the messages given to it.
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

    /**
     * Process this event from discord and determine if action is necessary.
     *
     * @return true if action on the server was taken, false otherwise
     */
    abstract suspend fun handleEvent(event: RuleBotEvent): Boolean

    abstract fun handlesCommand(name: String): Boolean

    /**
     * Process this event from *anywhere* and determine if action is necessary, or return information based on this request
     */
    open suspend fun configure(data: Any): Either<Exception, Any> = emptyMap<String, String>().right()

    @OverridingMethodsMustInvokeSuper
    open suspend fun onGuildCreate(context: GuildCreateContext) {
        logInfo { "Attaching commands" }
    }

    open suspend fun onSlashCommand(context: ChatInputInteractionContext) {}

    open suspend fun onUserCommand(context: UserInteractionContext) {}

    open suspend fun onButtonEvent(context: ButtonInteractionEventContext) {}

    abstract val priority: Priority

    protected open fun isAdminOnly() = true

    override fun equals(other: Any?): Boolean {
        if (other is Rule) {
            return other.ruleName == ruleName
        }
        return false
    }

//    protected suspend fun MessageCreated.canAuthorIssueRules(): Boolean {
//        val wrapper = getDiscordWrapperForEvent(this) ?: return false
//        val roles = wrapper.getRoles()
//        val isAdmin = roles.any { it.name == "Clown" }
//        val isOwner = wrapper.getGuildOwnerId() == author
//
//        logDebug { "checking if user is admin [$isAdmin] or isOwner [$isOwner]" }
//        return isAdmin || isOwner
//    }
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

private fun Message.sendDistortedCopy() {
    val content = this.content
    if (content.startsWith("<") && content.endsWith(">")) {
        return
    }
    if (content.length >= 12) {
        val distorted = distortText(content)
        this.channel
            .flatMap { it.createMessage(distorted) }
            .subscribe()
    }
}

private fun distortText(text: String): String {
    return text.map { char ->
        val random = java.util.Random().nextInt(9)
        if (random <= 3) {
            char.uppercaseChar()
        } else {
            char.lowercaseChar()
        }
    }.joinToString(separator = "") { it.toString() }
}

//Load the string content of a file from the resources
fun getStringFromResourceFile(filename: String): String {
    val classloader = Thread.currentThread().contextClassLoader
    val inputStream = classloader.getResourceAsStream(filename)!!
    return String(inputStream.readBytes()).trim()
}

fun getStringFromPath(filename: String): String {
    return File(filename).readText()
}

val client by lazy {
    HttpClient(Apache) {
        install(JsonFeature) {
            serializer = KotlinxSerializer(kotlinx.serialization.json.Json { ignoreUnknownKeys = true })
        }
    }
}