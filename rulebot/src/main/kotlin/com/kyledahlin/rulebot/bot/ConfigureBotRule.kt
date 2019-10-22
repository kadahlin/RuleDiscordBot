package com.kyledahlin.rulebot.bot

import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.util.Snowflake
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

    override suspend fun handleRule(messageEvent: MessageCreateEvent): Boolean {
        val message = messageEvent.message
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
            appendln("Configure properties of this rule com.kyledahlin.rulebot.bot")
            appendln("@ Mention the com.kyledahlin.rulebot.bot to use")
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