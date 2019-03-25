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
package jojorule

import Rule
import discord4j.core.`object`.entity.Message
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.get
import io.ktor.client.request.header
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import reactor.core.publisher.Mono

private const val RULE_PHRASE = "spicy jojo meme"

/**
 * Post one of the top images for the day from r/ShitPostCrusaders
 */
class JojoMemeRule : Rule("JoJoMeme") {

    //the fetched posts that have already posted while this rule has been alive
    private val mPostedIds = mutableSetOf<String>()

    override fun handleRule(message: Message): Mono<Boolean> {
        val doesContain = message.containsJojoRule()
        if (doesContain) {
            postJojoMemeFrom(message)
        }
        return Mono.just(doesContain)
    }

    override fun getExplanation(): String? {
        return StringBuilder().apply {
            append("Post a message with the phrase '$RULE_PHRASE'\n")
        }.toString()
    }

    private fun Message.containsJojoRule() = content.get().toLowerCase().contains(RULE_PHRASE)

    private fun postJojoMemeFrom(message: Message) = GlobalScope.launch {
        val redditResponse = client.get<RedditResponse>(JOJO_REDDIT) {
            header(
                "User-Agent",
                "JoJoMeme"
            )    //reddit api has some interesting behavior, see https://www.reddit.com/r/redditdev/comments/5w60r1/error_429_too_many_requests_i_havent_made_many/
        }
        val childList =
            redditResponse.data.children.children   //TODO: figure out kotlinx list deserialization a bit better

        val dataToPost = childList.firstOrNull {
            !mPostedIds.contains(it.data.id) && !it.data.is_video
        }?.data ?: return@launch

        mPostedIds.add(dataToPost.id)
        message.channel
            .flatMap { it.createMessage("${dataToPost.title}\n${dataToPost.url}") }
            .subscribe()
    }

    private val client by lazy {
        HttpClient(Apache) {
            install(JsonFeature) {
                serializer = KotlinxSerializer(Json.nonstrict)
            }
        }
    }
}