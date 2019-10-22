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
package com.kyledahlin.rulebot.bot.jojorule

import com.kyledahlin.rulebot.bot.LocalStorage
import com.kyledahlin.rulebot.bot.Rule
import com.kyledahlin.rulebot.bot.client
import discord4j.core.`object`.entity.Message
import discord4j.core.event.domain.message.MessageCreateEvent
import io.ktor.client.request.get
import io.ktor.client.request.header
import suspendChannel
import suspendCreateMessage
import java.io.File
import javax.inject.Inject

private const val RULE_PHRASE = "spicy jojo meme"
private const val JOJO_FILE_NAME = "jojo_id_file"

/**
 * Post one of the top images for the month from r/ShitPostCrusaders
 *
 * This rule will save to a file any post that it has already posted and will post unique images until the file is modified
 */
internal class JojoMemeRule @Inject constructor(storage: LocalStorage) : Rule("JoJoMeme", storage) {

    override val priority: Priority
        get() = Priority.LOW

    //the fetched posts that have already posted while this rule has been active
    private val mPostedIds = mutableSetOf<String>()

    init {
        val fileIds = getPostedIdsFromFile()
        logDebug("loading ${fileIds.size} items from the saved jojo file")
        mPostedIds.addAll(fileIds)
    }

    override suspend fun handleRule(messageEvent: MessageCreateEvent): Boolean {
        val message = messageEvent.message
        val doesContain = message.containsJojoRule()
        if (doesContain) {
            postJojoMemeFrom(message)
        }
        return doesContain
    }

    override fun getExplanation(): String? {
        return StringBuilder().apply {
            appendln("Post a message with the phrase '$RULE_PHRASE'")
        }.toString()
    }

    private fun Message.containsJojoRule() = content.orElse("").toLowerCase().contains(RULE_PHRASE)

    private suspend fun postJojoMemeFrom(message: Message) {
        val channel = message.channelId
        logDebug("jojo message from ${channel.asString()}")
        val redditResponse = client.get<RedditResponse>(JOJO_REDDIT) {
            header(
                "User-Agent",
                "JoJoMeme"
            )    //see https://www.reddit.com/r/redditdev/comments/5w60r1/error_429_too_many_requests_i_havent_made_many/
        }
        val childList =
            redditResponse.data.children.children   //TODO: figure out kotlinx list deserialization a bit better

        val dataToPost = childList.firstOrNull {
            !mPostedIds.contains(it.data.id) && !it.data.is_video
        }?.data ?: return

        saveIdToFile(dataToPost.id)

        mPostedIds.add(dataToPost.id)
        message.suspendChannel()?.suspendCreateMessage("${dataToPost.title}\n${dataToPost.url}")
    }

    @Synchronized
    private fun saveIdToFile(id: String) = File(JOJO_FILE_NAME).appendText("$id\n")

    @Synchronized
    private fun getPostedIdsFromFile() = try {
        File(JOJO_FILE_NAME)
            .readLines()
            .map { it.trim() }
            .filterNot { it.isEmpty() }
    } catch (e: Exception) {
        logError("error in loading IDS, ${e.stackTrace}")
        emptySet<String>()
    }
}