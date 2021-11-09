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
import com.kyledahlin.rulebot.ChatInputInteractionContext
import com.kyledahlin.rulebot.GuildCreateContext
import com.kyledahlin.rulebot.bot.Logger.logDebug
import com.kyledahlin.rulebot.bot.Logger.logInfo
import com.kyledahlin.rulebot.bot.Rule
import com.kyledahlin.rulebot.bot.RuleBotEvent
import discord4j.discordjson.json.ApplicationCommandRequest
import javax.inject.Inject

/**
 * Post one of the top images for the month from r/ShitPostCrusaders
 *
 * This rule will save to a file any post that it has already posted and will post unique images until the file is modified
 */
@MyRuleBotScope
internal class JojoMemeRule @Inject constructor(
    val storage: JojoMemeStorage,
    val _api: JojoApi,
    private val analytics: Analytics
) :
    Rule("jojo") {

    override val priority: Priority
        get() = Priority.LOW

    //the fetched posts that have already posted while this rule has been active
    private val _cachedIds = mutableMapOf<String, MutableSet<String>>()

    override suspend fun handleEvent(event: RuleBotEvent): Boolean {
        return false
    }

    override fun handlesCommand(name: String): Boolean {
        return name == ruleName
    }

    override suspend fun onGuildCreate(context: GuildCreateContext) {
        super.onGuildCreate(context)
        // Build our command's definition
        val greetCmdRequest = ApplicationCommandRequest.builder()
            .name("jojo")
            .description("Post a spicy jojo meme")
            .build()

        context.registerApplicationCommand(greetCmdRequest)
    }

    override suspend fun onSlashCommand(context: ChatInputInteractionContext) = measureExecutionTime("jojo meme") {

        logInfo { "handling slash for jojo" }
        context.deferReply()
        val posts = _api.getPosts()

        val guildId = context.channelId
        val dataToPost = posts.firstOrNull {
            !_cachedIds.getOrDefault(guildId.asString(), emptySet()).contains(it.data.id) && !it.data.isVideo
        }?.data ?: return@measureExecutionTime

        storage.saveIdToGuild(dataToPost.id, guildId)

        _cachedIds.getOrPut(guildId.asString()) { mutableSetOf() }.add(dataToPost.id)
        logDebug { "sending $dataToPost" }

        context.editReply {
            content { dataToPost.title }
            addEmbed {
                image { dataToPost.url }
            }
        }
    }
}