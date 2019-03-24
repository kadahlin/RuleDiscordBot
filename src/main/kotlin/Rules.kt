import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.User
import discord4j.core.`object`.reaction.ReactionEmoji
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface Rule {
    fun handleRule(message: Message): Mono<Boolean>
}

internal fun Message.getUsernames(): Flux<String> {
    return userMentions.map { it.username }
}

internal fun User.canIssueRules() = adminUsernames.any { it.username == this.username }

private fun Message.addReactionToMessage(emoji: ReactionEmoji) {
    this.channel.block()?.getMessageById(this.id)?.block()?.addReaction(emoji)?.block()
}

private fun Message.sendDistortedCopy() {
    val content = this.content.get()
    if (content.startsWith("<") && content.endsWith(">")) {
        return
    }
    if (content.length >= 12) {
        val distorted = distortText(content)
        this.channel.block()?.createMessage(distorted)?.block()
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