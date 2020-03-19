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
package com.kyledahlin.myrulebot.bot.timeoutrule

import com.kyledahlin.myrulebot.bot.MyRuleBotScope
import com.kyledahlin.rulebot.bot.*
import kotlinx.serialization.json.JsonObject
import javax.inject.Inject

private val timeoutRegex = """[0-9]+ minute timeout""".toRegex()

/**
 * Allow admins to timeout other users on the server
 *
 * Any message that a user on timeout types will be instantly deleted
 */
@MyRuleBotScope
internal class TimeoutRule @Inject constructor(
    storage: LocalStorage,
    val getDiscordWrapperForEvent: GetDiscordWrapperForEvent,
    val timeoutStorage: TimeoutStorage
) :
    Rule("Timeout", storage, getDiscordWrapperForEvent) {

    override val priority: Priority
        get() = Priority.HIGH

    override suspend fun handleEvent(event: RuleBotEvent): Boolean {
        if (event !is MessageCreated) return false
        val author = event.author
        if (processRemovalCommand(event)) {
            return true
        }
        val wrapper = getDiscordWrapperForEvent(event) ?: return true
        val existingTimeout = timeoutStorage.getTimeoutForSnowflake(author)
        if (existingTimeout != null) {
            if (existingTimeout.startTime + (existingTimeout.minutes * 60L * 1000L) > System.currentTimeMillis()) {
                //should delete message
                wrapper.deleteMessage()
                logDebug("user $author is still on timeout, deleting")
                return true
            } else {
                timeoutStorage.removeTimeoutForSnowflakes(setOf(author))
                logDebug("removing timeout for user: $author")
            }
        }
        if (!event.canAuthorIssueRules()) {
            return false
        }
        return processTimeoutCommand(event)
    }

    override fun getExplanation(): String? {
        return StringBuilder().apply {
            append("Set timeouts for people with a duration in minutes\n")
            append("To use: post a message that contains:\n")
            append("\t1. one or more @user to timeout\n")
            append("\t2. the phrase 'XX minute timeout' where XX is the duration of the timeout in minutes\n")
            append("To remove an existing timeout post a message that contains:\n")
            append("\t1. the word remove\n")
            append("\t2. the word timeout\n")
            append("\t3. the @user(s) to remove a timeout for\n")
        }.toString()
    }

    //true if their was a valid command to process
    private suspend fun processTimeoutCommand(event: MessageCreated): Boolean {
        if (!event.containsTimeoutCommand()) return false
        val duration = getDurationFromMessage(event) ?: return false

        val mentionedSnowflakes = event.snowflakes.filter { !it.isRole }.map { it.snowflake }

        timeoutStorage.removeTimeoutForSnowflakes(mentionedSnowflakes)
        val newTimeouts = mentionedSnowflakes.map { snowflake ->
            val timeout = Timeout(
                snowflake,
                System.currentTimeMillis(),
                duration
            )
            timeoutStorage.insertTimeouts(setOf(timeout))
            logInfo("adding timeout for user $snowflake at ${timeout.startTime} for ${timeout.minutes} minutes")
            timeout
        }
        if (newTimeouts.isNotEmpty()) {
            val wrapper = getDiscordWrapperForEvent(event)
            val noun = if (mentionedSnowflakes.size > 1) "their" else "his"
            val adminSnowflake = event.author.asLong()
            wrapper?.sendMessage("<@$adminSnowflake> sure thing chief, $noun timeout will end at ${newTimeouts[0].getFormattedEndDate()}")
        }
        return true
    }

    private suspend fun processRemovalCommand(event: MessageCreated): Boolean {
        val wrapper = getDiscordWrapperForEvent(event) ?: return false
        return if (event.containsRemovalCommand() && event.canAuthorIssueRules()) {
            val snowflakes = event.snowflakes.map { it.snowflake }
            timeoutStorage.removeTimeoutForSnowflakes(snowflakes)

            logInfo("removing the timeout for ${snowflakes.joinToString { it.asString() }}")
            val adminSnowflake = event.author.asLong()
            wrapper.sendMessage("<@$adminSnowflake> timeout removed")
            true
        } else
            false
    }

    private fun MessageCreated.containsTimeoutCommand(): Boolean {
        logDebug("testing '$content' for timeout command")
        return (com.kyledahlin.myrulebot.bot.timeoutrule.timeoutRegex.containsMatchIn(content)
                && snowflakes.isNotEmpty())
    }

    private fun MessageCreated.containsRemovalCommand(): Boolean {
        logDebug("testing '$content' for removal command")
        return (snowflakes.isNotEmpty()
                && content.contains("remove")
                && content.contains("timeout"))
    }

    private fun getDurationFromMessage(event: MessageCreated): Long? {
        val firstMatch = timeoutRegex.find(event.content)?.value ?: return null
        val minutes = firstMatch.split("\\s+".toRegex())[0]
        return minutes.toLong()
    }

    override suspend fun configure(data: Any): Any {
        return JsonObject(mapOf())
    }
}