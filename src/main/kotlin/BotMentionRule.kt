import discord4j.core.`object`.entity.Message
import reactor.core.publisher.Mono

//Respond to the user if they mention honk bot and its not kyle

class BotMentionRule : Rule {

    override fun handleRule(message: Message): Mono<Boolean> {
        return message.getUsernames()
            .filter { it == bot.username }
            .collectList()
            .flatMap {
                val isNotEmpty = it.isNotEmpty()
                if (isNotEmpty) {
                    message.channel
                        .flatMap { channel -> channel.createMessage("Dont mention ${bot.username} directly. I haven't added code for this yet but I will") }
                        .subscribe()
                }
                Mono.just(isNotEmpty)
            }
    }
}