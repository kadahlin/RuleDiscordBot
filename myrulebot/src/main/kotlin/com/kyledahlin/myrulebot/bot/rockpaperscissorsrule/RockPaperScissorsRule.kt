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
package com.kyledahlin.myrulebot.bot.rockpaperscissorsrule

import com.kyledahlin.myrulebot.bot.MyRuleBotScope
import com.kyledahlin.rulebot.EventWrapper
import com.kyledahlin.rulebot.bot.*
import discord4j.core.`object`.util.Snowflake
import javax.inject.Inject
import kotlin.random.Random

private enum class RpsChoice {
    ROCK, PAPER, SCISSORS;

    infix fun winsAgainst(otherChoice: RpsChoice): Boolean? = when {
        this == ROCK -> when (otherChoice) {
            ROCK -> null
            PAPER -> false
            SCISSORS -> true
        }
        this == PAPER -> when (otherChoice) {
            ROCK -> true
            PAPER -> null
            SCISSORS -> false
        }
        else -> when (otherChoice) {
            ROCK -> false
            PAPER -> true
            SCISSORS -> null
        }
    }
}

/**
 * Simulate a rock paper scissors game against one of the users in chat.
 *
 * Maintains a record of games won / lost for each player
 */
@MyRuleBotScope
internal class RockPaperScissorsRule @Inject constructor(
    private val getDiscordWrapperForEvent: GetDiscordWrapperForEvent,
    private val getBotIds: GetBotIds,
    private val rockPaperScissorsStorage: RockPaperScissorsStorage
) :
    Rule("RockPaperScissors", getDiscordWrapperForEvent) {

    override val priority: Priority
        get() = Priority.LOW

    override suspend fun handleEvent(event: RuleBotEvent): Boolean {
        if (event !is MessageCreated) return false
        val content = event.content
        logDebug("testing '$content' for rps command")
        if (!content.startsWith("rps")) {
            return false
        }
        handleRpsCommand(event)
        return true
    }

    private suspend fun handleRpsCommand(event: MessageCreated) {
        val contentPieces = event.content.split(" ")
        val wrapper = getDiscordWrapperForEvent(event) ?: return
        if (contentPieces.size != 2) {
            wrapper.sendMessage("missing a choice")
            return
        }

        val playerChoice = getChoiceFromString(contentPieces[1])
        if (playerChoice == null) {
            wrapper.sendMessage("invalid choice")
            return
        }

        val playerSnowflake = event.author

        val botChoice = getRandomChoice()
        val didPlayerWin = playerChoice winsAgainst botChoice
        var draw = false
        var winner = getBotIds().first()
        if (didPlayerWin == null) {
            draw = true
        } else if (didPlayerWin) {
            winner = playerSnowflake
        }

        val game = RockPaperScissorGame(
            participant1 = playerSnowflake,
            participant2 = getBotIds().first(),
            winner = winner,
            draw = draw
        )
        rockPaperScissorsStorage.insertRpsGame(game)
        printAllGamesForPlayer(wrapper, playerSnowflake, botChoice, didPlayerWin)
    }

    private suspend fun printAllGamesForPlayer(
        wrapper: EventWrapper,
        snowflake: Snowflake,
        botChoice: RpsChoice,
        didPlayerWin: Boolean?
    ) {
        val games = rockPaperScissorsStorage.getAllRpsGamesForPlayer(snowflake)
        games.forEach {
            println("queried $it")
        }
        val totalNonDrawGames = games.size
        val wonGames = games.count { it.winner == snowflake && !it.draw }
        val totalDraws = games.count { it.draw }
        val resultMessage = when (didPlayerWin) {
            true -> "You Won"
            false -> "You Lose"
            null -> "Draw"
        }
        val stringMessage =
            "${
                botChoice.name.toLowerCase()
                    .capitalize()
            }! $resultMessage! You have won $wonGames out of $totalNonDrawGames and have had $totalDraws game(s) end in a draw"
        wrapper.sendMessage(stringMessage)
    }

    private fun getChoiceFromString(content: String): RpsChoice? = when (content) {
        "r", "rock" -> RpsChoice.ROCK
        "p", "paper" -> RpsChoice.PAPER
        "s", "scissors" -> RpsChoice.SCISSORS
        else -> null
    }

    private fun getRandomChoice() = when (Random.nextInt(3)) {
        0 -> RpsChoice.ROCK
        1 -> RpsChoice.PAPER
        2 -> RpsChoice.SCISSORS
        else -> RpsChoice.ROCK
    }

    override fun getExplanation() = StringBuilder().apply {
        appendLine("Start a message with 'rps'")
        appendLine("the next word needs to be either rock, paper, scissors or r,p,s")
    }.toString()


}

data class RockPaperScissorGame(
    val participant1: Snowflake,
    val participant2: Snowflake,
    val winner: Snowflake,
    val draw: Boolean
)