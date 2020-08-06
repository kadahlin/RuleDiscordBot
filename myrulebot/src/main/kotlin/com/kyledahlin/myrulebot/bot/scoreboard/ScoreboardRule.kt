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
package com.kyledahlin.myrulebot.bot.scoreboard

import com.kyledahlin.myrulebot.bot.MyRuleBotScope
import com.kyledahlin.rulebot.bot.*
import kotlinx.serialization.json.JsonObject
import java.io.FileInputStream
import javax.inject.Inject

private const val SCOREBOARD_CREATE = "scoreboard-create"
private const val SCOREBOARD_ADD_PLAYER = "scoreboard-add-player"
private const val SCOREBOARD_ADD_WIN = "scoreboard-add-win"
private const val SCOREBOARD_SHOW = "scoreboard-show"
private val NAME = """name=[a-zA-Z]+""".toRegex()
private val PLAYER = """player=[a-zA-Z]+""".toRegex()

/**
 * Create and update 'scoreboards' that tracks different games with members.
 *
 * Each member of a scoreboard will be assigned a wins value that can be incremented with chat commands
 */
@MyRuleBotScope
internal class ScoreboardRule @Inject constructor(
    storage: LocalStorage,
    val getDiscordWrapperForEvent: GetDiscordWrapperForEvent,
    val scoreboardStorage: ScoreboardStorage
) :
    Rule("Scoreboard", storage, getDiscordWrapperForEvent) {

    override val priority: Priority
        get() = Priority.LOW

    override suspend fun handleEvent(event: RuleBotEvent): Boolean {
        if (event !is MessageCreated) return false

        if (!event.content.containsScoreboardCommand()) {
            logDebug("content ${event.content} does not contain a scoreboard command")
            return false
        }
        handleScoreboardCommand(event)
        return true
    }

    private suspend fun handleScoreboardCommand(event: MessageCreated) {
        val content = event.content
        val returnMessage = when {
            content.startsWith(SCOREBOARD_CREATE)     -> createScoreboard(content, event)
            content.startsWith(SCOREBOARD_ADD_PLAYER) -> addPlayer(content, event)
            content.startsWith(SCOREBOARD_ADD_WIN)    -> addWin(content, event)
            content.startsWith(SCOREBOARD_SHOW)       -> showScoreboard(content, event)
            else                                      -> "invalid scoreboard command"
        }
        logDebug("return message was $returnMessage")
        if (!returnMessage.isNullOrEmpty()) {
            getDiscordWrapperForEvent(event)?.sendMessage(returnMessage)
        }
    }

    private suspend fun createScoreboard(content: String, event: MessageCreated): String {
        logDebug("start creating scoreboard")
        val scoreboardName = content.getNameValue() ?: return "missing scoreboard name"
        val exists = scoreboardStorage.getScoreboardIdForName(scoreboardName) != null
        if (exists) {
            return "this scoreboard name already exists"
        }

        scoreboardStorage.insertScoreboard(scoreboardName, event.author)

        return "Scoreboard $scoreboardName has been created"
    }

    private suspend fun addPlayer(content: String, event: MessageCreated): String {
        logDebug("start adding player")
        val scoreboardName = content.getNameValue() ?: return "missing scoreboard name"
        val playerName = content.getPlayerValue() ?: return "missing player name"

        val scoreboardId =
            scoreboardStorage.getScoreboardIdForName(scoreboardName) ?: return "scoreboard does not exist"
        val exists = scoreboardStorage.doesScoreboardHavePlayer(scoreboardId, playerName)

        if (exists) {
            return "this player already exists for this game"
        }

        scoreboardStorage.addPlayer(scoreboardId, playerName, wins = 0)
        return "Player $playerName has been added to $scoreboardName"
    }

    private suspend fun addWin(content: String, event: MessageCreated): String {
        logDebug("start adding win")
        val author = event.author
        val scoreboardName = content.getNameValue() ?: return "missing scoreboard name"
        val playerName = content.getPlayerValue() ?: return "missing player name"

        val scoreboardId =
            scoreboardStorage.getScoreboardIdForName(scoreboardName) ?: return "this scoreboard does not exist"

        val hasPlayer = scoreboardStorage.doesScoreboardHavePlayer(scoreboardId, playerName)
        if (hasPlayer) {
            scoreboardStorage.giveWinToPlayer(scoreboardId, playerName)
        } else {
            scoreboardStorage.addPlayer(scoreboardId, playerName, wins = 1)
        }

        return "giving win to player $playerName"
    }

    private suspend fun showScoreboard(content: String, event: MessageCreated): String? {
        val scoreboardName = content.getNameValue() ?: return "missing scoreboard name"

        val scoreboardId = scoreboardStorage.getScoreboardIdForName(scoreboardName)
            ?: return "this scoreboard does not exist"

        val playerChartPoints = scoreboardStorage.getPlayersForScoreboard(scoreboardId)
            .map { (name, wins) ->
                ChartPoint(label = name, value = wins)
            }

        val filename = generateWinChart(playerChartPoints, scoreboardName)
            ?: return "nothing to show for this scoreboard, possibly no players?"

        val inputStream = FileInputStream(filename)
        val wrapper = getDiscordWrapperForEvent(event)
        wrapper?.sendMessage {
            addFile(filename, inputStream)
        }
        return null
    }

    override fun getExplanation(): String? {
        return StringBuilder().apply {
            appendln("Create and update scoreboards made of player names and wins")
            appendln("$SCOREBOARD_CREATE name=<name>")
            appendln("\tCreate a new scoreboard with the given name")
            appendln("$SCOREBOARD_ADD_PLAYER name=<name> player=<name>")
            appendln("\tAdd this player to the given scoreboard (owner only command)")
            appendln("$SCOREBOARD_ADD_WIN name=<name> player=<name>")
            appendln(
                "\tGive a win to this player on this scoreboard (owner only command)\n\tIf this player does not exist for this scoreboard they will be created"
            )
            appendln("$SCOREBOARD_SHOW name=<name>")
            appendln("\tShow the scoreboard with the given name")
        }.toString()
    }

    override suspend fun configure(data: Any): Any {
        return JsonObject(mapOf())
    }

    private fun String.containsScoreboardCommand() = this.startsWith(
        SCOREBOARD_CREATE
    )
            || this.startsWith(SCOREBOARD_ADD_PLAYER)
            || this.startsWith(SCOREBOARD_ADD_WIN)
            || this.startsWith(SCOREBOARD_SHOW)

    private fun String.getNameValue() = NAME.find(this)?.value?.split("=")?.get(1)?.toLowerCase()?.capitalize()

    private fun String.getPlayerValue() = PLAYER.find(this)?.value?.split("=")?.get(1)?.toLowerCase()?.capitalize()

}