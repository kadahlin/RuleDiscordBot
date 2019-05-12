package bot

import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.util.Snowflake
import reactor.core.publisher.Mono

private val setAdminRegex = """add admin""".toRegex()
private val removeAdminRegex = """remove admin""".toRegex()
private val listAdminsRegex = """list admins""".toRegex()

internal class ConfigureBotRule(botSnowflakes: Set<Snowflake>, private val storage: LocalStorage) :
    Rule("ConfigureBot", storage) {

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
            appendln("Configure properties of this rule bot")
            appendln("@ Mention the bot to use")
            appendln("Commands:")
            appendln("\t1. add admin <username> or <role>")
            appendln("\t\tadd this role to the 'admin' category and allow access commands")
            appendln("\t2. list admins")
            appendln("\t3. remove admin <username> or <role>")
        }.toString()
    }

    private fun executeRule(message: Message) {

        message.content.ifPresent {
            val content = message.content.get()
            when {
                setAdminRegex.containsMatchIn(content) -> setAdmin(message)
                removeAdminRegex.containsMatchIn(content) -> removeAdmin(message)
                listAdminsRegex.containsMatchIn(content) -> listAdmins(message)
            }
        }

    }

    private fun setAdmin(message: Message) {
        val adminSnowflakes = storage.getAdminSnowflakes()
        val newAdmins = message
            .getSnowflakes()
            .filter {
                !mBotSnowflakes.contains(it.snowflake) || !adminSnowflakes.contains(it)
            }
        logDebug("adding ${newAdmins.joinToString(separator = ",") { it.snowflake.asString() }} to the admin list")
        storage.addAdminSnowflakes(newAdmins.toSet())
    }

    private fun removeAdmin(message: Message) {
        val adminsToRemove = message.getSnowflakes().map { it.snowflake }
            .filterNot { mBotSnowflakes.contains(it) }
        logDebug("removing ${adminsToRemove.joinToString(separator = ",") { it.asString() }} from admin list")
        storage.removeAdminSnowflakes(adminsToRemove)
    }

    private fun listAdmins(message: Message) {
        val admins = storage.getAdminSnowflakes()
        val usermentions = admins.map {
            if (it.isRole) "<@&${it.snowflake.asString()}>" else "<@${it.snowflake.asString()}>"
        }
        message.channel
            .flatMap {
                it.createMessage("Admins are: ${usermentions.joinToString(separator = " ")}")
            }.subscribe()
    }
}