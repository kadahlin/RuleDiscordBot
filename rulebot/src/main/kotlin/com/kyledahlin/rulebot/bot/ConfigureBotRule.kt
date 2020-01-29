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

import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.util.Snowflake
import discord4j.core.event.domain.Event
import discord4j.core.event.domain.message.MessageCreateEvent
import suspendChannel
import suspendCreateMessage
import javax.inject.Inject

private val setAdminRegex = """add admin""".toRegex()
private val removeAdminRegex = """remove admin""".toRegex()
private val listAdminsRegex = """list admins""".toRegex()

internal class ConfigureBotRule @Inject constructor(botSnowflakes: Set<Snowflake>, private val storage: LocalStorage) :
    Rule("ConfigureBot", storage) {

    override val priority: Priority
        get() = Priority.NORMAL

    private val mBotSnowflakes = mutableSetOf<Snowflake>()

    init {
        mBotSnowflakes.addAll(botSnowflakes)
    }

    override suspend fun handleEvent(event: Event): Boolean {
        if (event !is MessageCreateEvent) return false
        val message = event.message
        if (!message.canAuthorIssueRules()) {
            return false
        }
        val mentionsBot = message.getSnowflakes()
            .map { it.snowflake }
            .any { mBotSnowflakes.contains(it) }

        if (mentionsBot) {
            executeRule(message)
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

    private suspend fun executeRule(message: Message) {
        val content = message.content.get()
        when {
            setAdminRegex.containsMatchIn(content) -> setAdmin(message)
            removeAdminRegex.containsMatchIn(content) -> removeAdmin(message)
            listAdminsRegex.containsMatchIn(content) -> listAdmins(message)
        }

    }

    private suspend fun setAdmin(message: Message) {
        val adminSnowflakes = storage.getAdminSnowflakes()
        val newAdmins = message
            .getSnowflakes()
            .filter {
                !mBotSnowflakes.contains(it.snowflake) || !adminSnowflakes.contains(it)
            }
        logDebug("adding ${newAdmins.joinToString(separator = ",") { it.snowflake.asString() }} to the admin list")
        storage.addAdminSnowflakes(newAdmins.toSet())
    }

    private suspend fun removeAdmin(message: Message) {
        val adminsToRemove = message.getSnowflakes().map { it.snowflake }
            .filterNot { mBotSnowflakes.contains(it) }
        logDebug("removing ${adminsToRemove.joinToString(separator = ",") { it.asString() }} from admin list")
        storage.removeAdminSnowflakes(adminsToRemove)
    }

    private suspend fun listAdmins(message: Message) {
        val admins = storage.getAdminSnowflakes()
        val usermentions = admins.map {
            if (it.isRole) "<@&${it.snowflake.asString()}>" else "<@${it.snowflake.asString()}>"
        }
        message.suspendChannel()?.suspendCreateMessage("Admins are: ${usermentions.joinToString(separator = " ")}")
    }
}