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
package com.kyledahlin.rulebot.bot

import discord4j.core.`object`.util.Snowflake
import javax.inject.Inject

private val setAdminRegex = """add admin""".toRegex()
private val removeAdminRegex = """remove admin""".toRegex()
private val listAdminsRegex = """list admins""".toRegex()

internal class ConfigureBotRule @Inject constructor(
    botSnowflakes: Set<Snowflake>,
    private val storage: LocalStorage,
    val getDiscordWrapperForEvent: GetDiscordWrapperForEvent
) :
    Rule("ConfigureBot", storage, getDiscordWrapperForEvent) {

    override val priority: Priority
        get() = Priority.NORMAL

    private val _botSnowflakes = mutableSetOf<Snowflake>()

    init {
        _botSnowflakes.addAll(botSnowflakes)
    }

    override suspend fun handleEvent(event: RuleBotEvent): Boolean {
        if (event !is MessageCreated) return false
        if (!event.canAuthorIssueRules()) {
            return false
        }
        logDebug("this message mentions [${event.snowflakes.map { it.snowflake }}] and the bot snowflakes are [${_botSnowflakes}]")
        val mentionsBot = event.snowflakes
            .map { it.snowflake }
            .any { _botSnowflakes.contains(it) }

        logDebug("does this event mention the bot? [$mentionsBot]")

        if (mentionsBot) {
            executeRule(event)
        }
        return mentionsBot
    }

    override fun getExplanation(): String? {
        return StringBuilder().apply {
            appendln("Configure properties of this rule bot")
            appendln("@ Mention the bot to use")
            appendln("Commands:")
            appendln("\t1. add admin <username> or <role>")
            appendln("\t\tadd this role to the 'admin' category and allow access commands")
            appendln("\t2. list admins")
            appendln("\t3. remove admin <username> or <role>")
        }.toString()
    }

    private suspend fun executeRule(messageCreated: MessageCreated) {
        when {
            setAdminRegex.containsMatchIn(messageCreated.content) -> setAdmin(messageCreated)
            removeAdminRegex.containsMatchIn(messageCreated.content) -> removeAdmin(messageCreated)
            listAdminsRegex.containsMatchIn(messageCreated.content) -> listAdmins(messageCreated)
        }
    }

    private suspend fun setAdmin(event: MessageCreated) {
        val adminSnowflakes = storage.getAdminSnowflakes()
        val newAdmins = event
            .snowflakes
            .filter {
                !_botSnowflakes.contains(it.snowflake) || !adminSnowflakes.contains(it)
            }
        logDebug("adding ${newAdmins.joinToString(separator = ",") { it.snowflake.asString() }} to the admin list")
        storage.addAdminSnowflakes(newAdmins.toSet())
    }

    private suspend fun removeAdmin(event: MessageCreated) {
        val guildId = getDiscordWrapperForEvent(event)?.getGuildId() ?: return
        val adminsToRemove = event
            .snowflakes
            .filterNot { _botSnowflakes.contains(it.snowflake) }
        logDebug("removing ${adminsToRemove.joinToString(separator = ",")} from admin list")
        storage.removeAdminSnowflakes(adminsToRemove, guildId)
    }

    private suspend fun listAdmins(event: MessageCreated) {
        val wrapper = getDiscordWrapperForEvent(event) ?: return
        val admins = storage.getAdminSnowflakes().filter {
            it.guildSnowflake == wrapper.getGuildId()
        }
        val userMentions = admins.map {
            if (it.isRole) "<@&${it.snowflake.asString()}>" else "<@${it.snowflake.asString()}>"
        }
        getDiscordWrapperForEvent(event)?.sendMessage("Admins are: ${userMentions.joinToString(separator = " ")}")
    }
}