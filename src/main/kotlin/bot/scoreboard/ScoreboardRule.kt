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
package bot.scoreboard

import bot.LocalStorage
import bot.Rule
import discord4j.core.`object`.entity.Message
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import reactor.core.publisher.Mono

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
internal class ScoreboardRule(storage: LocalStorage) : Rule("Scoreboard", storage) {

    override fun handleRule(message: Message): Mono<Boolean> {
        val content = try {
            message.content.get()
        } catch (e: Exception) {
            logError("error on getting scoreboard content ${e.message}")
            return Mono.just(false)
        }

        if (!content.containsScoreboardCommand()) {
            logDebug("content $content does not contain a scoreboard command")
            return Mono.just(false)
        }
        handleScoreboardCommand(message)
        return Mono.just(true)
    }

    private fun handleScoreboardCommand(message: Message) {
        val content = message.content.get()
        val returnMessage = when {
            content.startsWith(SCOREBOARD_CREATE) -> createScoreboard(content, message)
            content.startsWith(SCOREBOARD_ADD_PLAYER) -> addPlayer(content, message)
            content.startsWith(SCOREBOARD_ADD_WIN) -> addWin(content, message)
            content.startsWith(SCOREBOARD_SHOW) -> showScoreboard(content)
            else -> "invalid scoreboard command"
        }
        logDebug("return message was $returnMessage")
        message.channel.subscribe { channel ->
            channel.createMessage(returnMessage).subscribe()
        }
    }

    private fun createScoreboard(content: String, message: Message): String = transaction {
        logDebug("start creating scoreboard")
        val author = message.author.get()
        val scoreboardName = content.getNameValue() ?: return@transaction "missing scoreboard name"
        val count = Scoreboards.select { Scoreboards.name eq scoreboardName }.count()
        if (count != 0) {
            return@transaction "this scoreboard name already exists"
        }

        Scoreboards.insert {
            it[name] = scoreboardName
            it[ownerSnowflake] = author.id.asString()
        }

        "Scoreboard $scoreboardName has been created"
    }

    private fun addPlayer(content: String, message: Message): String = transaction {
        logDebug("start adding player")
        val author = message.author.get()
        val scoreboardName = content.getNameValue() ?: return@transaction "missing scoreboard name"
        val playerName = content.getPlayerValue() ?: return@transaction "missing player name"

        val scoreboardQuery = Scoreboards.slice(Scoreboards.id, Scoreboards.ownerSnowflake)
            .select { Scoreboards.name eq scoreboardName }
            .firstOrNull() ?: return@transaction "this scoreboard does not exist"

        val scoreboardId = scoreboardQuery[Scoreboards.id]
        if (author.id.asString() != scoreboardQuery[Scoreboards.ownerSnowflake]) {
            return@transaction "You are not the owner of this scoreboard"
        }

        val existingCount = ScoreboardPlayers.select {
            ScoreboardPlayers.scoreboardId eq scoreboardId and (ScoreboardPlayers.name eq playerName)
        }.count()

        if (existingCount != 0) {
            return@transaction "this player already exists for this game"
        }

        ScoreboardPlayers.insert {
            it[ScoreboardPlayers.name] = playerName
            it[ScoreboardPlayers.scoreboardId] = scoreboardId
            it[wins] = 0
        }

        "Player $playerName has been added to $scoreboardName"
    }

    private fun addWin(content: String, message: Message): String = transaction {
        logDebug("start adding win")
        val author = message.author.get()
        val scoreboardName = content.getNameValue() ?: return@transaction "missing scoreboard name"
        val playerName = content.getNameValue() ?: return@transaction "missing player name"

        val scoreboardQuery = Scoreboards.slice(Scoreboards.id, Scoreboards.ownerSnowflake)
            .select { Scoreboards.name eq scoreboardName }
            .firstOrNull() ?: return@transaction "this scoreboard does not exist"

        val scoreboardId = scoreboardQuery[Scoreboards.id]
        if (author.id.asString() != scoreboardQuery[Scoreboards.ownerSnowflake]) {
            return@transaction "You are not the owner of this scoreboard"
        }

        val existingCount = ScoreboardPlayers.select {
            ScoreboardPlayers.scoreboardId eq scoreboardId and (ScoreboardPlayers.name eq playerName)
        }.count()

        if (existingCount != 0) {
            ScoreboardPlayers.update({ ScoreboardPlayers.scoreboardId eq scoreboardId and (ScoreboardPlayers.name eq playerName) }) {
                with(SqlExpressionBuilder) {
                    it.update(ScoreboardPlayers.wins, ScoreboardPlayers.wins + 1)
                }
            }
        } else {
            ScoreboardPlayers.insert {
                it[ScoreboardPlayers.name] = playerName
                it[ScoreboardPlayers.wins] = 1
                it[ScoreboardPlayers.scoreboardId] = scoreboardId
            }
        }

        "giving win to player $playerName"
    }

    private fun showScoreboard(content: String): String = transaction {
        val scoreboardName = content.getNameValue() ?: return@transaction "missing scoreboard name"
        val scoreboardQuery = Scoreboards.slice(Scoreboards.id)
            .select { Scoreboards.name eq scoreboardName }
            .firstOrNull() ?: return@transaction "this scoreboard does not exist"

        val scoreboardId = scoreboardQuery[Scoreboards.id]
        val playerStats = ScoreboardPlayers.select {
            ScoreboardPlayers.scoreboardId eq scoreboardId
        }.joinToString(separator = ", ") {
            "${it[ScoreboardPlayers.name]}:${it[ScoreboardPlayers.wins]}"
        }

        if (playerStats.isEmpty()) "No members for this board" else playerStats
    }

    override fun getExplanation(): String? {
        return StringBuilder().apply {
            appendln("Create and update scoreboards made of player names and wins")
            appendln("$SCOREBOARD_CREATE name=<name>")
            appendln("\tCreate a new scoreboard with the given name")
            appendln("$SCOREBOARD_ADD_PLAYER name=<name> player=<name>")
            appendln("\tAdd this player to the given scoreboard (owner only command)")
            appendln("$SCOREBOARD_ADD_WIN name=<name> player=<name>")
            appendln("\tGive a win to this player on this scoreboard (owner only command)\n\tIf this player does not exist for this scoreboard they will be created")
            appendln("$SCOREBOARD_SHOW name=<name>")
            appendln("\tShow the scoreboard with the given name")
        }.toString()
    }

    private fun String.containsScoreboardCommand() = this.startsWith(SCOREBOARD_CREATE)
            || this.startsWith(SCOREBOARD_ADD_PLAYER)
            || this.startsWith(SCOREBOARD_ADD_WIN)
            || this.startsWith(SCOREBOARD_SHOW)

    private fun String.getNameValue() = NAME.find(this)?.value?.split("=")?.get(1)?.toLowerCase()?.capitalize()

    private fun String.getPlayerValue() = PLAYER.find(this)?.value?.split("=")?.get(1)?.toLowerCase()?.capitalize()

}

object Scoreboards : Table() {
    val id = integer("id").primaryKey().autoIncrement()
    val ownerSnowflake = varchar("snowflake", 64)
    val name = varchar("name", 30)
}

object ScoreboardPlayers : Table() {
    val name = varchar("name", 30).primaryKey(0)
    val wins = integer("wins")
    val scoreboardId = integer("scoreboard_id").references(Scoreboards.id, ReferenceOption.CASCADE).primaryKey(1)
}