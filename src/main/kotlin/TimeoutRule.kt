import discord4j.core.`object`.entity.Message
import reactor.core.publisher.Mono
import java.text.SimpleDateFormat
import java.util.*

class Timeout(
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
        val date = Date(startTime + (minutes * 60 * 1000))
        val format = SimpleDateFormat("hh:mm")
        return format.format(date)
    }
}

private val timeoutRegex = """[0-9]+ minute timeout""".toRegex()

class TimeoutRule : Rule {

    //Map of userId ->
    private val mTimeouts = mutableSetOf<Timeout>()

    override fun handleRule(message: Message): Mono<Boolean> {
        val author = message.author.get().username
        if (message.containsRemovalCommand().block()!! && message.author.get().canIssueRules()) {
            message.getUsernames().collectList().subscribe { usernames ->
                mTimeouts.removeAll { usernames.contains(it.username) }
                println("removing the timeout for ${usernames.joinToString { it }}")
            }
            return Mono.just(true)
        }
        val existingTimeout = mTimeouts.firstOrNull { it.username == author }
        if (existingTimeout != null) {
            if (existingTimeout.startTime + (existingTimeout.minutes * 60 * 1000) > System.currentTimeMillis()) {
                //should delete message
                message.delete().subscribe()
                println("user $author is still on timeout, deleting")
            } else {
                mTimeouts.removeIf { it.username == author }
                println("removing timeout for user: $author")
            }
        }
        if (!message.author.get().canIssueRules()) {
            return Mono.just(false)
        }
        return Mono.just(processTimeoutCommand(message))
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
                    println("adding timeout for user $it at ${timeout.startTime} for ${timeout.minutes} minutes")
                    timeout
                }.last()
                message.channel
                    .flatMap { channel ->
                        val noun = if (usernames.size > 1) "their" else "his"
                        val adminUsername = message.author.get().username
                        val adminSnowflake = adminUsernames.first { it.username == adminUsername }
                        channel.createMessage("<@$adminSnowflake> sure thing chief, $noun timeout will end at ${lastTimeout.format()}")
                    }.subscribe()
            }
        return true
    }

    private fun Message.containsTimeoutCommand(): Mono<Boolean> {
        val content = this.content.get()
        println("testing $content for timeout command")
        return this.getUsernames().collectList().flatMap { usernames ->
            val validTimeoutCommand = content.contains(bot.username)
                    && timeoutRegex.containsMatchIn(content)
                    && usernames.isNotEmpty()
            Mono.just(validTimeoutCommand)
        }
    }

    private fun Message.containsRemovalCommand(): Mono<Boolean> {
        val content = this.content.get()
        println("testing $content for removal command")
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