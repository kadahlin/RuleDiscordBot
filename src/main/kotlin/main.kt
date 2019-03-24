import discord4j.core.DiscordClientBuilder
import discord4j.core.`object`.util.Snowflake
import discord4j.core.event.domain.lifecycle.ReadyEvent
import discord4j.core.event.domain.message.MessageCreateEvent

//Kyle Dahlin 2019

private lateinit var mId: Snowflake
private lateinit var mRules: Set<Rule>

fun main() {
    mRules = setOf(TimeoutRule(), BotMentionRule())
    val client = DiscordClientBuilder(getToken()).build()

    client.eventDispatcher.on(ReadyEvent::class.java)
        .subscribe { ready ->
            System.out.println("Logged in as " + ready.self.username)
            mId = ready.self.id
        }

    client.eventDispatcher.on(MessageCreateEvent::class.java)
        .map { msg -> msg.message }
        .subscribe { msg ->
            val username = msg.author.get().username
            println("message from $username")
            println("content is ${msg.content.get()}")
            if (msg.author.get().username != bot.username) {
                mRules.any {
                    it.handleRule(msg).block()!!
                }
            }
        }

    client.login().block()
}

private fun getToken(): String {
    val classloader = Thread.currentThread().contextClassLoader
    val inputStream = classloader.getResourceAsStream("token.txt")
    val token = String(inputStream.readBytes()).trim()
    println("returning token $token")
    return token
}