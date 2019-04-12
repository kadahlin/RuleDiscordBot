package bot

import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.util.Snowflake
import reactor.core.publisher.Mono

private val setAdminRegex = """set admin""".toRegex()
private val removeAdminRegex = """remove admin""".toRegex()
private val listAdminsRegex = """list admins""".toRegex()

internal class ConfigureBotRule(botSnowflakes: Set<Snowflake>) : Rule("ConfigureBot") {

    private val mBotSnowflakes = mutableSetOf<Snowflake>()

    init {
        mBotSnowflakes.addAll(botSnowflakes)
    }

    override fun handleRule(message: Message): Mono<Boolean> {
        if (!message.canAuthorIssueRules().block()!!) {
            return Mono.just(false)
        }
        val mentionsBot = message.getSnowflakes()
            .map { it.snowflake }
            .any {
                mBotSnowflakes.contains(it)
            }
        if (mentionsBot) {
            executeRule(message)
        }
        return Mono.just(mentionsBot)
    }

    override fun getExplanation(): String? {
        return StringBuilder().apply {
            append("Configure properties of this rule bot\n")
            append("@ Mention the bot to use\n")
            append("Possible commands:\n")
            append("\t1. set admin role <@role>\n")
            append("\t\tadd this role to the 'admin' category and allow access commands\n")
        }.toString()
    }

    private fun executeRule(message: Message) {

        val content = message.content.get()
        when {
            setAdminRegex.containsMatchIn(content) -> setAdmin(message)
            removeAdminRegex.containsMatchIn(content) -> removeAdmin(message)
            listAdminsRegex.containsMatchIn(content) -> listAdmins(message)
        }
    }

    private fun setAdmin(message: Message) {
        val adminSnowflakes = getAdminSnowflakes()
        val newAdmins = message
            .getSnowflakes()
            .filter {
                !mBotSnowflakes.contains(it.snowflake) || !adminSnowflakes.contains(it)
            }
        logDebug("adding ${newAdmins.joinToString(separator = ",") { it.snowflake.asString() }} to the admin list")
        addAdminSnowflakes(newAdmins.toSet())
    }

    private fun removeAdmin(message: Message) {
        val adminsToRemove = message.getSnowflakes().map { it.snowflake }
            .filterNot { mBotSnowflakes.contains(it) }
        logDebug("removing ${adminsToRemove.joinToString(separator = ",") { it.asString() }} from admin list")
        removeAdminSnowflakes(adminsToRemove)
    }

    private fun listAdmins(message: Message) {
        val admins = getAdminSnowflakes()
        val usermentions = admins.map {
            if (it.isRole) "<@&${it.snowflake.asString()}>" else "<@${it.snowflake.asString()}>"
        }
        message.channel
            .flatMap {
                it.createMessage("Admins are: ${usermentions.joinToString(separator = " ")}")
            }.subscribe()
    }
}