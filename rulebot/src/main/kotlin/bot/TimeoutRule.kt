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
import discord4j.core.`object`.util.Snowflake
import discord4j.core.event.domain.message.MessageCreateEvent
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import suspendChannel
import suspendCreateMessage
import suspendDelete
import suspendUserMentions
import java.text.SimpleDateFormat
import java.util.*

internal object Timeouts : IntIdTable() {
    val snowflake = varchar("snowflake", 64)
    val startTime = long("startTime")
    val duration = long("duration")
}

private data class Timeout(
    val snowflake: Snowflake,
    val startTime: Long,
    val minutes: Long
) {

    override fun equals(other: Any?): Boolean {
        if (other is Timeout) {
            return other.snowflake == this.snowflake
        }
        return false
    }

    fun getFormattedEndDate(): String {
        val date = Date(startTime + (minutes * 60L * 1000L))
        val format = SimpleDateFormat("hh:mm MMMM dd, YYYY", Locale.US)
        return format.format(date)
    }
}

private val timeoutRegex = """[0-9]+ minute timeout""".toRegex()

/**
 * Allow admins to timeout other users on the server
 *
 * Any message that a user on timeout types will be instantly deleted
 */
internal class TimeoutRule(storage: LocalStorage) : Rule("Timeout", storage) {

    override suspend fun handleRule(messageEvent: MessageCreateEvent): Boolean {
        val message = messageEvent.message
        val author = message.author.get()
        if (processRemovalCommand(message)) {
            return true
        }
        val existingTimeout = getTimeoutForSnowflake(author.id)
        if (existingTimeout != null) {
            if (existingTimeout.startTime + (existingTimeout.minutes * 60L * 1000L) > System.currentTimeMillis()) {
                //should delete message
                message.suspendDelete()
                logDebug("user $author is still on timeout, deleting")
                return true
            } else {
                removeTimeoutForSnowflakes(setOf(author.id))
                logDebug("removing timeout for user: $author")
            }
        }
        if (!message.canAuthorIssueRules()) {
            return false
        }
        return processTimeoutCommand(message)
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
    private suspend fun processTimeoutCommand(message: Message): Boolean {
        if (!message.containsTimeoutCommand()) return false
        val duration = getDurationFromMessage(message) ?: return false

        val mentionedSnowflakes = message.suspendUserMentions()?.map { it.id } ?: emptySet<Snowflake>()

        removeTimeoutForSnowflakes(mentionedSnowflakes)
        val newTimeouts = mentionedSnowflakes.map { snowflake ->
            val timeout = Timeout(snowflake, System.currentTimeMillis(), duration)
            insertTimeouts(setOf(timeout))
            logInfo("adding timeout for user $snowflake at ${timeout.startTime} for ${timeout.minutes} minutes")
            timeout
        }
        if (newTimeouts.isNotEmpty()) {
            val channel = message.suspendChannel()
            val noun = if (mentionedSnowflakes.size > 1) "their" else "his"
            val adminSnowflake = message.author.get().id.asLong()
            channel?.suspendCreateMessage("<@$adminSnowflake> sure thing chief, $noun timeout will end at ${newTimeouts[0].getFormattedEndDate()}")
        }
        return true
    }

    private suspend fun processRemovalCommand(message: Message): Boolean {
        return if (message.containsRemovalCommand() && message.canAuthorIssueRules()) {
            val snowflakes = message.getSnowflakes().map { it.snowflake }
            removeTimeoutForSnowflakes(snowflakes)

            logInfo("removing the timeout for ${snowflakes.joinToString { it.asString() }}")
            val adminSnowflake = message.author.get().id.asLong()
            message.suspendChannel()?.suspendCreateMessage("<@$adminSnowflake> timeout removed")
            true
        } else
            false
    }

    private fun Message.containsTimeoutCommand(): Boolean {
        val content = content.orElse("")
        logDebug("testing '$content' for timeout command")
        val usernames = getSnowflakes()
        return (timeoutRegex.containsMatchIn(content)
                && usernames.isNotEmpty())
    }

    private fun Message.containsRemovalCommand(): Boolean {
        val content = content.orElse("")
        logDebug("testing '$content' for removal command")
        val usernames = getSnowflakes()
        return (usernames.isNotEmpty()
                && content.contains("remove")
                && content.contains("timeout"))
    }

    private fun getDurationFromMessage(message: Message): Long? {
        val content = message.content.orElse("")
        val firstMatch = timeoutRegex.find(content)?.value ?: return null
        val minutes = firstMatch.split("\\s+".toRegex())[0]
        return minutes.toLong()
    }

    private fun insertTimeouts(timeouts: Collection<Timeout>) {
        transaction {
            for (timeout in timeouts) {
                Timeouts.insert {
                    it[snowflake] = timeout.snowflake.asString()
                    it[duration] = timeout.minutes
                    it[startTime] = timeout.startTime
                }
            }
        }
    }

    private fun removeTimeoutForSnowflakes(snowflakes: Collection<Snowflake>) {
        transaction {
            Timeouts.deleteWhere {
                Timeouts.snowflake inList snowflakes.map { it.asString() }
            }
        }
    }

    private fun getTimeoutForSnowflake(snowflake: Snowflake): Timeout? = transaction {
        val existing = Timeouts.select { Timeouts.snowflake eq snowflake.asString() }
            .firstOrNull() ?: return@transaction null

        Timeout(snowflake, existing[Timeouts.startTime], existing[Timeouts.duration])
    }
}