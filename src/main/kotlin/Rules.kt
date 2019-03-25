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
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.User
import discord4j.core.`object`.reaction.ReactionEmoji
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * A self contained piece of logic that operates on the messages given to it.
 */
abstract class Rule(private val ruleName: String) {
    /**
     * Process this message and determine if action is necessary.
     *
     * @return true if action on the server was taken, false otherwise
     */
    abstract fun handleRule(message: Message): Mono<Boolean>

    /**
     * Log information that is only useful when debugging
     */
    protected fun logDebug(logMessage: String) {
        Logger.logDebug("[$ruleName] $logMessage")
    }

    /**
     * Log information that is useful when seeing past actions
     */
    protected fun logInfo(logMessage: String) {
        Logger.logInfo("[$ruleName] $logMessage")
    }

    /**
     * Log information related to a program error
     */
    protected fun logError(logMessage: String) {
        Logger.logError("[$ruleName] $logMessage")
    }
}

internal fun Message.getUsernames(): Flux<String> {
    return userMentions.map { it.username }
}

internal fun User.canIssueRules() = adminUsernames.any { it.username == this.username }

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