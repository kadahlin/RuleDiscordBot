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
import reactor.core.publisher.Mono
import java.text.SimpleDateFormat
import java.util.*

private class Timeout(
    val username: String,
    val startTime: Long,
    val minutes: Int
) {
    override fun equals(other: Any?): Boolean {
        if (other is Timeout) {
            return other.username == this.username
        }
        return false
    }

    fun format(): String {
        val date = Date(startTime + (minutes.toLong() * 60L * 1000L))
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
internal class TimeoutRule : Rule("Timeout") {

    //Map of userId ->
    private val mTimeouts = mutableSetOf<Timeout>()

    override fun handleRule(message: Message): Mono<Boolean> {
        val author = message.author.get().username
        if (processRemovalCommand(message)) {
            return Mono.just(true)
        }
        val existingTimeout = mTimeouts.firstOrNull { it.username == author }
        if (existingTimeout != null) {
            if (existingTimeout.startTime + (existingTimeout.minutes * 60 * 1000) > System.currentTimeMillis()) {
                //should delete message
                message.delete().subscribe()
                logDebug("user $author is still on timeout, deleting")
            } else {
                mTimeouts.removeIf { it.username == author }
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
            append("\t1. the phrase ${bot.username}\n")
            append("\t2. a @user to timeout\n")
            append("\t3. the phrase 'XX minute timeout' where XX is the duration of the timeout in minutes\n")
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
            .map { it.username }
            .collectList()
            .subscribe { usernames ->
                mTimeouts.removeIf { usernames.contains(it.username) }
                val lastTimeout = usernames.map {
                    val timeout = Timeout(it, System.currentTimeMillis(), duration)
                    mTimeouts.add(timeout)
                    logInfo("adding timeout for user $it at ${timeout.startTime} for ${timeout.minutes} minutes")
                    timeout
                }.last()
                message.channel
                    .flatMap { channel ->
                        val noun = if (usernames.size > 1) "their" else "his"
                        val adminUsername = message.author.get().username
                        val adminSnowflake = adminUsernames.first { it.username == adminUsername }
                        channel.createMessage("<@${adminSnowflake.id}> sure thing chief, $noun timeout will end at ${lastTimeout.format()}")
                    }.subscribe()
            }
        return true
    }

    private fun processRemovalCommand(message: Message): Boolean {
        return if (message.containsRemovalCommand().block()!! && message.author.get().canIssueRules()) {
            message.getUsernames().collectList().subscribe { usernames ->
                mTimeouts.removeAll { usernames.contains(it.username) }
                logInfo("removing the timeout for ${usernames.joinToString { it }}")
                val adminUsername = message.author.get().username
                val adminSnowflake = adminUsernames.first { it.username == adminUsername }
                message.channel.block()?.createMessage("<@${adminSnowflake.id}> timeout removed")?.subscribe()
            }
            true
        } else
            false
    }

    private fun Message.containsTimeoutCommand(): Mono<Boolean> {
        val content = this.content.get()
        logDebug("testing $content for timeout command")
        return this.getUsernames().collectList().flatMap { usernames ->
            val validTimeoutCommand = content.contains(bot.username)
                    && timeoutRegex.containsMatchIn(content)
                    && usernames.isNotEmpty()
            Mono.just(validTimeoutCommand)
        }
    }

    private fun Message.containsRemovalCommand(): Mono<Boolean> {
        val content = this.content.get()
        logDebug("testing $content for removal command")
        return this.getUsernames().collectList().flatMap { usernames ->
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
}