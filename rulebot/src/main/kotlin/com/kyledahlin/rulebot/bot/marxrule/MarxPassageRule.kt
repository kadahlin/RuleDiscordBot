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
package com.kyledahlin.rulebot.bot.marxrule

import com.kyledahlin.rulebot.bot.*
import java.io.File
import java.util.*
import javax.inject.Inject

private const val PASSAGE_FILE_NAME = "KARL_condensed.txt"
private const val DELIMITER = "||"
private const val TRIGGER = "bluepill me"

/**
 * Post a random passage (paragraph with at least 5 lines) from capital
 */
internal class MarxPassageRule @Inject constructor(
    storage: LocalStorage,
    private val getDiscordWrapperForEvent: GetDiscordWrapperForEvent
) : Rule("MarxPassages", storage, getDiscordWrapperForEvent) {

    private val random = Random()

    override val priority: Priority
        get() = Priority.LOW

    override suspend fun handleEvent(event: RuleBotEvent): Boolean {
        if (event !is MessageCreated) return false

        return if (event.content.contains(TRIGGER)) {
            val passage = getPassage()
            getDiscordWrapperForEvent(event)?.sendMessage(passage)
            true
        } else {
            false
        }
    }

    private suspend fun getPassage(): String {
        val passages = getStringFromResourceFile("KARL_condensed.txt").split(DELIMITER)
        val passage = passages[random.nextInt(passages.size)]
        return passage
    }

    override fun getExplanation(): String? {
        return "Post a random passage from Karl Marx's 'Capital Volume One'\nSimply post a message with the phrase $TRIGGER to receive a selection\n"
    }
}