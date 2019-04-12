/*
*Copyright 2019 Kyle Dahlin
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
package bot

import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.reaction.ReactionEmoji
import discord4j.core.`object`.util.Snowflake
import reactor.bool.logicalOr
import reactor.core.publisher.Mono

/**
 * A self contained piece of logic that operates on the messages given to it.
 */
abstract class Rule(internal val ruleName: String) {
    /**
     * Process this message and determine if action is necessary.
     *
     * @return true if action on the server was taken, false otherwise
     */
    abstract fun handleRule(message: Message): Mono<Boolean>

    /**
     * Get a human readable description of how to use this rule
     */
    abstract fun getExplanation(): String?

    protected open fun isAdminOnly() = true

    /**
     * Log information that is only useful when debugging
     */
    protected fun logDebug(logMessage: String) {
        Logger.logDebug("[${ruleName}bot.Rule] $logMessage")
    }

    /**
     * Log information that is useful when seeing past actions
     */
    protected fun logInfo(logMessage: String) {
        Logger.logInfo("[${ruleName}bot.Rule] $logMessage")
    }

    /**
     * Log information related to a program error
     */
    protected fun logError(logMessage: String) {
        Logger.logError("[${ruleName}Rule] $logMessage")
    }

    override fun equals(other: Any?): Boolean {
        if (other is Rule) {
            return other.ruleName == ruleName
        }
        return false
    }
}

internal data class RoleSnowflake(
    val snowflake: Snowflake,
    val isRole: Boolean = false
)

//return all users and roles that were mentioned in this message
internal fun Message.getSnowflakes(): Set<RoleSnowflake> {
    val users = userMentionIds.map { RoleSnowflake(it) }
    val roles = roleMentionIds.map { RoleSnowflake(it, isRole = true) }
    return users.union(roles)
}

internal fun Message.canAuthorIssueRules(): Mono<Boolean> {
    val isAdmin = this.guild
        .flatMap { author.get().asMember(it.id) }
        .map { it.roleIds }
        .flatMap { userRoles ->
            val admins = getAdminSnowflakes().map { it.snowflake }
            val isAllowed = userRoles.any {
                admins.contains(it)
            }
            Mono.just(isAllowed)
        }

    val isOwner = this.guild
        .flatMap { it.owner }
        .map { it.id }
        .flatMap {
            Mono.just(it.asString() == author.get().id.asString())
        }

    return isAdmin.logicalOr(isOwner)
}

private fun Message.addReactionToMessage(emoji: ReactionEmoji) {
    this.channel
        .flatMap { it.getMessageById(this.id) }
        .flatMap { it.addReaction(emoji) }
        .subscribe()
}

private fun Message.sendDistortedCopy() {
    val content = this.content.get()
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
            char.toUpperCase()
        } else {
            char.toLowerCase()
        }
    }.joinToString(separator = "") { it.toString() }
}

//Load the discord bot.getBot secret token from resources
internal fun getTokenFromFile(filename: String): String {
    val classloader = Thread.currentThread().contextClassLoader
    val inputStream = classloader.getResourceAsStream(filename)
    return String(inputStream.readBytes()).trim()
}