package com.kyledahlin.rulebot.bot

import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.util.Snowflake
import discord4j.core.event.domain.message.MessageCreateEvent
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import suspendChannel
import suspendCreateMessage
import javax.inject.Inject
import kotlin.random.Random

private enum class RpsChoice {
    ROCK, PAPER, SCISSORS;

    infix fun winAgainst(otherChoice: RpsChoice): Boolean? = when {
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

internal class RockPaperScissorsRule @Inject constructor(private val botIds: Set<Snowflake>, storage: LocalStorage) :
    Rule("RockPaperScissors", storage) {

    override val priority: Priority
        get() = Priority.LOW

    override suspend fun handleRule(messageEvent: MessageCreateEvent): Boolean {
        val message = messageEvent.message
        val content = message.content.orElse("")
        logDebug("testing '$content' for rps command")
        if (!content.startsWith("rps")) {
            return false
        }
        handleRpsCommand(message, content)
        return true
    }

    private suspend fun handleRpsCommand(message: Message, content: String) {
        val contentPieces = content.split(" ")
        if (contentPieces.size != 2) {
            message.suspendChannel()?.suspendCreateMessage("missing a choice")
            return
        }

        val playerChoice = getChoiceFromString(contentPieces[1])
        if (playerChoice == null) {
            message.suspendChannel()?.suspendCreateMessage("invalid choice")
            return
        }

        val playerSnowflake = message.author.get().id

        val botChoice = getRandomChoice()
        val didPlayerWin = playerChoice winAgainst botChoice
        var draw = false
        var winner = botIds.first()
        if (didPlayerWin == null) {
            draw = true
        } else if (didPlayerWin) {
            winner = playerSnowflake
        }

        val game = RockPaperScissorGame(
            participant1 = playerSnowflake,
            participant2 = botIds.first(),
            winner = winner,
            draw = draw
        )
        insertNewRpsGame(game)
        printAllGamesForPlayer(message, playerSnowflake, botChoice, didPlayerWin)
    }

    private suspend fun printAllGamesForPlayer(
        message: Message,
        snowflake: Snowflake,
        botChoice: RpsChoice,
        didPlayerWin: Boolean?
    ) {
        val games = getAllRpsGamesForPlayer(snowflake)
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
            "${botChoice.name.toLowerCase().capitalize()}! $resultMessage! You have won $wonGames out of $totalNonDrawGames and have had $totalDraws game(s) end in a draw"
        message.suspendChannel()?.suspendCreateMessage(stringMessage)
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

    private fun insertNewRpsGame(rpsGame: RockPaperScissorGame) = transaction {
        println("inserting $rpsGame")
        RockPaperScissorGames.insert {
            it[participant1] = rpsGame.participant1.asString()
            it[participant2] = rpsGame.participant2.asString()
            it[winner] = rpsGame.winner.asString()
            it[draw] = rpsGame.draw
        }
    }

    private fun getAllRpsGamesForPlayer(snowflake: Snowflake): Collection<RockPaperScissorGame> = transaction {
        RockPaperScissorGames.select { RockPaperScissorGames.participant1 eq snowflake.asString() or (RockPaperScissorGames.participant2 eq snowflake.asString()) }
            .map {
                val participant1 = it[RockPaperScissorGames.participant1]
                val participant2 = it[RockPaperScissorGames.participant2]
                val winner = it[RockPaperScissorGames.winner]
                val draw = it[RockPaperScissorGames.draw]
                RockPaperScissorGame(Snowflake.of(participant1), Snowflake.of(participant2), Snowflake.of(winner), draw)
            }.toList()
    }

    override fun getExplanation() = StringBuilder().apply {
        appendln("Start a message with 'rps'")
        appendln("the next word needs to be either rock, paper, scissors or r,p,s")
    }.toString()
}

data class RockPaperScissorGame(
    val participant1: Snowflake,
    val participant2: Snowflake,
    val winner: Snowflake,
    val draw: Boolean
)

object RockPaperScissorGames : IntIdTable() {
    val participant1 = varchar("participant1", 64)
    val participant2 = varchar("participant2", 64)
    val winner = varchar("winner", 64)
    val draw = bool("draw")
}