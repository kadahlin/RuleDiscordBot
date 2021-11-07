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
import com.kyledahlin.rulebot.ButtonInteractionEventContext
import com.kyledahlin.rulebot.GuildCreateContext
import com.kyledahlin.rulebot.UserInteractionContext
import com.kyledahlin.rulebot.bot.Logger.logDebug
import com.kyledahlin.rulebot.bot.Rule
import com.kyledahlin.rulebot.bot.RuleBotEvent
import discord4j.common.util.Snowflake
import discord4j.core.`object`.component.ActionRow
import discord4j.core.`object`.component.Button
import discord4j.discordjson.json.ApplicationCommandRequest
import java.util.*
import javax.inject.Inject


enum class RpsChoice {
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

    override fun toString(): String {
        return name.lowercase(Locale.getDefault())
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }
}

private const val rps = "rockpaperscissors"
private const val challenge = "Challenge RPS"

data class GameState(
    val id: UUID,
    val playerOne: Snowflake,
    val playerTwo: Snowflake,
    val originalChannel: Snowflake,
    val originalGuild: Snowflake
) {
    var choiceOne: RpsChoice? = null
    var choiceTwo: RpsChoice? = null

    override fun equals(other: Any?): Boolean {
        return other is GameState && other.id == id
    }
}

/**
 * Simulate a rock paper scissors game against one of the users in chat.
 *
 * Maintains a record of games won / lost for each player
 */
@MyRuleBotScope
internal class RockPaperScissorsRule @Inject constructor(
    private val rockPaperScissorsStorage: RockPaperScissorsStorage
) :
    Rule(rps) {

    private val games = mutableSetOf<GameState>()

    override val priority: Priority
        get() = Priority.LOW

    override suspend fun handleEvent(event: RuleBotEvent): Boolean {
        return false
    }

    override fun handlesCommand(name: String): Boolean {
        return name == rps || name == challenge
    }

    override suspend fun onUserCommand(context: UserInteractionContext) {
        val targetUser = context.targetUser
        val firstUser = context.user
        val gameId = UUID.randomUUID()
        games.add(
            GameState(
                gameId,
                firstUser.first,
                targetUser.first,
                context.channelId,
                context.guildId
            )
        )

        // send the initiator's choice
        context.reply {
            content { "Make your choice" }
            withEphemeral()
            addComponent {
                ActionRow.of(
                    listOf(
                        "${gameId}_one_rock" to "Rock",
                        "${gameId}_one_paper" to "Paper",
                        "${gameId}_one_scissors" to "Scissors"
                    ).map {
                        Button.primary(it.first, it.second)
                    })
            }
        }

        context.sendMessageToTargetUser {
            content(
                "${firstUser.second} challenged you to Rock Paper Scissors"
            )
            addComponent(
                ActionRow.of(
                    listOf(
                        "${gameId}_two_rock" to "Rock",
                        "${gameId}_two_paper" to "Paper",
                        "${gameId}_two_scissors" to "Scissors"
                    ).map {
                        Button.primary(it.first, it.second)
                    })
            )
        }
    }

    override suspend fun onButtonEvent(context: ButtonInteractionEventContext) {
        val (player, id, choice) = getIdAndChoice(context.customId) ?: return
        logDebug { "button reply for $id and $choice and $player" }
        val game = games.firstOrNull { it.id == id } ?: return
        if (player == "one") {
            game.choiceOne = choice
        } else {
            game.choiceTwo = choice
        }
        context.reply {
            content { "Submitted $choice" }
            withEphemeral()
        }
        if (game.choiceOne != null && game.choiceTwo != null) {
            endGame(game)
        }
    }

    // TODO: figure out why tuple3 is missing
    private fun getIdAndChoice(customId: String): arrow.core.Tuple4<String, UUID, RpsChoice, Unit>? {
        val index = customId.lastIndexOf("_")
        if (index == -1) {
            return null
        }

        val uuidString = customId.substring(0, index - 4)
        val player = customId.substring(index - 3, index)

        val rpsChoice = when (customId.substring(index + 1, customId.length)) {
            "rock" -> RpsChoice.ROCK
            "paper" -> RpsChoice.PAPER
            "scissors" -> RpsChoice.SCISSORS
            else -> throw IllegalArgumentException("cant be here")
        }
        val uuid = UUID.fromString(uuidString)
        return arrow.core.Tuple4(player, uuid, rpsChoice, Unit)
    }

    override suspend fun onGuildCreate(context: GuildCreateContext) {
        super.onGuildCreate(context)
        val userRequest = ApplicationCommandRequest.builder()
            .name(challenge)
            .type(2)
            .build()

        context.registerApplicationCommand(userRequest)
    }

    private suspend fun endGame(game: GameState) {
        logDebug { "ending game $game" }
        val playerTwoName =
            context.getUsernameInGuild(game.playerTwo, game.originalGuild)
        val playerOneName =
            context.getUsernameInGuild(game.playerOne, game.originalGuild)
        val (resultMessage, winner) = when (game.choiceOne!! winsAgainst game.choiceTwo!!) {
            true -> "$playerOneName wins" to game.playerOne
            false -> "$playerTwoName wins" to game.playerTwo
            null -> "Draw! Try again next time" to game.playerOne
        }
        rockPaperScissorsStorage.insertRpsGame(
            game.originalGuild,
            game.playerOne,
            game.playerTwo,
            winner,
            (game.choiceOne!! winsAgainst game.choiceTwo!!) == null
        )
        logDebug { "game inserted successfully" }

        val games = rockPaperScissorsStorage.getAllRpsGamesForPlayer(game.playerOne)
        val totalNonDrawGames = games.size
        val wonGames = games.count { it.winner == game.playerOne && !it.draw }
        val totalDraws = games.count { it.draw }

        val content =
            "$playerOneName played Rock Paper Scissors!. He sent ${game.choiceOne} and $playerTwoName sent ${game.choiceTwo}. $resultMessage! $playerOneName has won $wonGames out of $totalNonDrawGames games played."
        context.sendMessageToChannel(game.originalChannel) {
            content(content)
        }
    }
}