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

    private val mBotSnowflakes = mutableSetOf<Snowflake>()

    init {
        mBotSnowflakes.addAll(botSnowflakes)
    }

    override suspend fun handleEvent(event: RuleBotEvent): Boolean {
        if (event !is MessageCreated) return false
        if (!event.canAuthorIssueRules()) {
            return false
        }
        val mentionsBot = event.snowflakes
            .map { it.snowflake }
            .any { mBotSnowflakes.contains(it) }

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

    private suspend fun executeRule(event: RuleBotEvent) {
        if (event !is MessageCreated) return
        when {
            setAdminRegex.containsMatchIn(event.content) -> setAdmin(event)
            removeAdminRegex.containsMatchIn(event.content) -> removeAdmin(event)
            listAdminsRegex.containsMatchIn(event.content) -> listAdmins(event)
        }

    }

    private suspend fun setAdmin(event: MessageCreated) {
        val adminSnowflakes = storage.getAdminSnowflakes()
        val newAdmins = event
            .snowflakes
            .filter {
                !mBotSnowflakes.contains(it.snowflake) || !adminSnowflakes.contains(it)
            }
        logDebug("adding ${newAdmins.joinToString(separator = ",") { it.snowflake.asString() }} to the admin list")
        storage.addAdminSnowflakes(newAdmins.toSet())
    }

    private suspend fun removeAdmin(event: MessageCreated) {
        val adminsToRemove = event.snowflakes.map { it.snowflake }
            .filterNot { mBotSnowflakes.contains(it) }
        logDebug("removing ${adminsToRemove.joinToString(separator = ",") { it.asString() }} from admin list")
        storage.removeAdminSnowflakes(adminsToRemove)
    }

    private suspend fun listAdmins(event: MessageCreated) {
        val admins = storage.getAdminSnowflakes()
        val userMentions = admins.map {
            if (it.isRole) "<@&${it.snowflake.asString()}>" else "<@${it.snowflake.asString()}>"
        }
        getDiscordWrapperForEvent(event)?.sendMessage("Admins are: ${userMentions.joinToString(separator = " ")}")
    }
}