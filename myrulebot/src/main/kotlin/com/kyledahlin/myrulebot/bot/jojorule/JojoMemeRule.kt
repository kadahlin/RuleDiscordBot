/*
*Copyright 2020 Kyle Dahlin
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
package com.kyledahlin.myrulebot.bot.jojorule

import com.kyledahlin.myrulebot.bot.MyRuleBotScope
import com.kyledahlin.rulebot.Analytics
import com.kyledahlin.rulebot.bot.*
import discord4j.common.util.Snowflake
import io.ktor.client.request.*
import javax.inject.Inject

private const val RULE_PHRASE = "spicy jojo meme"
private const val JOJO_FILE_NAME = "jojo_id_file"

/**
 * Post one of the top images for the month from r/ShitPostCrusaders
 *
 * This rule will save to a file any post that it has already posted and will post unique images until the file is modified
 */
@MyRuleBotScope
internal class JojoMemeRule @Inject constructor(
    val getDiscordWrapperForEvent: GetDiscordWrapperForEvent,
    val storage: JojoMemeStorage,
    private val analytics: Analytics
) :
    Rule("JoJoMeme", getDiscordWrapperForEvent) {

    override val priority: Priority
        get() = Priority.LOW

    //the fetched posts that have already posted while this rule has been active
    private val mPostedIds = mutableMapOf<Snowflake, MutableSet<String>>()

    override suspend fun handleEvent(event: RuleBotEvent): Boolean {
        if (event !is MessageCreated) return false
        val doesContain = event.containsJojoRule()
        if (doesContain) {
            postJojoMemeFrom(event)
        }
        return doesContain
    }

    override fun getExplanation(): String? {
        return StringBuilder().apply {
            appendLine("Post a message with the phrase '$RULE_PHRASE'")
        }.toString()
    }

    private fun MessageCreated.containsJojoRule() = content.toLowerCase().contains(
        RULE_PHRASE
    )

    private suspend fun postJojoMemeFrom(event: MessageCreated) {
        val wrapper = getDiscordWrapperForEvent(event) ?: return
        val guildId = wrapper.getGuildId() ?: return

        if (mPostedIds[guildId] == null) {
            mPostedIds[guildId] = storage.getIdsForServer(guildId).toMutableSet()
        }

        val redditResponse = client.get<RedditResponse>(
            JOJO_REDDIT
        ) {
            header(
                "User-Agent",
                "JoJoMeme"
            )    //see https://www.reddit.com/r/redditdev/comments/5w60r1/error_429_too_many_requests_i_havent_made_many/
        }
        val childList =
            redditResponse.data.children   //TODO: figure out kotlinx list deserialization a bit better

        val dataToPost = childList.firstOrNull {
            !mPostedIds.getOrDefault(guildId, emptySet()).contains(it.data.id) && !it.data.isVideo
        }?.data ?: return

        storage.saveIdToGuild(dataToPost.id, guildId)

        mPostedIds[guildId]?.add(dataToPost.id)
        wrapper.sendMessage("${dataToPost.title}\n${dataToPost.url}")
    }
}