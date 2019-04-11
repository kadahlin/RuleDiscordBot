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
import reactor.core.publisher.Mono
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

private const val TIMEOUT_FILE_NAME = "timeout_file"
private const val SEPARATOR = ","

private class Timeout(
    val snowflake: Snowflake,
    val startTime: Long,
    val minutes: Int
) {
    override fun equals(other: Any?): Boolean {
        if (other is Timeout) {
            return other.snowflake == this.snowflake
        }
        return false
    }

    fun format(): String {
        val date = Date(startTime + (minutes.toLong() * 60L * 1000L))
        val format = SimpleDateFormat("hh:mm MMMM dd, YYYY", Locale.US)
        return format.format(date)
    }

    override fun toString(): String {
        return arrayOf(snowflake.asString(), startTime, minutes).joinToString(separator = SEPARATOR) { it.toString() }
    }

    companion object {
        fun fromString(serialized: String): Timeout {
            val pieces = serialized.split(SEPARATOR)
            return Timeout(Snowflake.of(pieces[0]), pieces[1].toLong(), pieces[2].toInt())
        }
    }
}

private val timeoutRegex = """[0-9]+ minute timeout""".toRegex()

/**
 * Allow admins to timeout other users on the server
 *
 * Any message that a user on timeout types will be instantly deleted
 */
internal class TimeoutRule : Rule("Timeout") {

    //Map of userId ->
    private val mTimeouts = mutableSetOf<Timeout>()

    init {
        val fileTimeouts = loadTimeoutsFromFile()
        logDebug("there were ${fileTimeouts.size} timeouts in the file")
        mTimeouts.addAll(fileTimeouts)
    }

    override fun handleRule(message: Message): Mono<Boolean> {
        val author = message.author.get()
        if (processRemovalCommand(message)) {
            return Mono.just(true)
        }
        val existingTimeout = mTimeouts.firstOrNull { it.snowflake == author.id }
        if (existingTimeout != null) {
            if (existingTimeout.startTime + (existingTimeout.minutes.toLong() * 60L * 1000L) > System.currentTimeMillis()) {
                //should delete message
                message.delete().subscribe()
                logDebug("user $author is still on timeout, deleting")
            } else {
                mTimeouts.removeIf { it.snowflake == author.id }
                removeTimeoutFromFile(existingTimeout.snowflake)
                logDebug("removing timeout for user: $author")
            }
        }
        if (!message.author.get().canIssueRules()) {
            return Mono.just(false)
        }
        return Mono.just(processTimeoutCommand(message))
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
    private fun processTimeoutCommand(message: Message): Boolean {
        if (!message.containsTimeoutCommand().block()!!) return false
        val duration = getDurationFromMessage(message) ?: return false
        message.userMentions
            .map { it.id }
            .collectList()
            .subscribe { snowflakes ->
                mTimeouts.removeIf { snowflakes.contains(it.snowflake) }
                snowflakes.forEach { removeTimeoutFromFile(it) }
                val lastTimeout = snowflakes.map { snowflake ->
                    val timeout = Timeout(snowflake, System.currentTimeMillis(), duration)
                    mTimeouts.add(timeout)
                    addTimeoutToFile(timeout)
                    logInfo("adding timeout for user $snowflake at ${timeout.startTime} for ${timeout.minutes} minutes")
                    timeout
                }.last()
                message.channel
                    .flatMap { channel ->
                        val noun = if (snowflakes.size > 1) "their" else "his"
                        val adminSnowflake = message.author.get().id.asLong()
                        channel.createMessage("<@$adminSnowflake> sure thing chief, $noun timeout will end at ${lastTimeout.format()}")
                    }.subscribe()
            }
        return true
    }

    private fun processRemovalCommand(message: Message): Boolean {
        return if (message.containsRemovalCommand().block()!! && message.author.get().canIssueRules()) {
            message.getSnowflakes().collectList().subscribe { snowflakes ->
                mTimeouts.removeAll { snowflakes.contains(it.snowflake) }
                snowflakes.forEach { removeTimeoutFromFile(it) }
                logInfo("removing the timeout for ${snowflakes.joinToString { it.asString() }}")
                val adminSnowflake = message.author.get().id.asLong()
                message.channel.block()?.createMessage("<@$adminSnowflake> timeout removed")?.subscribe()
            }
            true
        } else
            false
    }

    private fun Message.containsTimeoutCommand(): Mono<Boolean> {
        val content = this.content.get()
        logDebug("testing '$content' for timeout command")
        return this.getSnowflakes().collectList().flatMap { usernames ->
            val validTimeoutCommand = timeoutRegex.containsMatchIn(content)
                    && usernames.isNotEmpty()
            Mono.just(validTimeoutCommand)
        }
    }

    private fun Message.containsRemovalCommand(): Mono<Boolean> {
        val content = this.content.get()
        logDebug("testing '$content' for removal command")
        return this.getSnowflakes().collectList().flatMap { usernames ->
            val isValidRemovalCommand = usernames.isNotEmpty()
                    && content.contains("remove")
                    && content.contains("timeout")
            Mono.just(isValidRemovalCommand)
        }
    }

    private fun getDurationFromMessage(message: Message): Int? {
        val content = message.content.get()
        val firstMatch = timeoutRegex.find(content)?.value ?: return null
        val minutes = firstMatch.split("\\s+".toRegex())[0]
        return minutes.toInt()
    }

    private fun loadTimeoutsFromFile(): Collection<Timeout> {
        return try {
            File(TIMEOUT_FILE_NAME)
                .readLines()
                .map { it.trim() }
                .map { Timeout.Companion.fromString(it) }
        } catch (e: Exception) {
            logError("exception on reading timeout file: ${e.stackTrace}")
            emptySet()
        }
    }

    private fun removeTimeoutFromFile(snowflake: Snowflake) = try {
        val file = File(TIMEOUT_FILE_NAME)
        val newFileContent = file
            .readLines()
            .filterNot { it.startsWith(snowflake.asString()) }
            .joinToString { it }

        file.writeText(newFileContent)
    } catch (e: Exception) {
        logError("error on removing from timeout file: ${e.stackTrace}")
    }

    private fun addTimeoutToFile(timeout: Timeout) {
        File(TIMEOUT_FILE_NAME).appendText("$timeout\n")
    }
}