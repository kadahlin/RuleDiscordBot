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
import reactor.core.publisher.Mono

//Respond to the user if they mention honk bot.getBot and its not kyle

class BotMentionRule : Rule("BotMention") {

    override fun handleRule(message: Message): Mono<Boolean> {
        return message.getSnowflakes(filterBot = false)
            .filter { it.asLong() == bot.snowflake }
            .collectList()
            .flatMap {
                val isNotEmpty = it.isNotEmpty()
                if (isNotEmpty) {
                    message.channel
//                        .flatMap { channel -> channel.createMessage("Dont mention ${bot.getBot.username} directly. I haven't added code for this yet but I will") }
                        .subscribe()
                }
                Mono.just(isNotEmpty)
            }
    }

    override fun getExplanation(): String? = null

    override fun isAdminOnly() = false
}