package com.kyledahlin.myrulebot.bot.keyvalue

import com.kyledahlin.myrulebot.bot.MyRuleBotScope
import com.kyledahlin.rulebot.analytics.Analytics
import com.kyledahlin.rulebot.bot.*
import com.kyledahlin.rulebot.sf
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.json.JsonObject
import javax.inject.Inject

@MyRuleBotScope
internal class KeyValueRule @Inject constructor(
    private val storage: KeyValueRuleStorage,
    localStorage: LocalStorage,
    private val getDiscordWrapper: GetDiscordWrapperForEvent,
    private val analytics: Analytics
) :
    Rule("KeyValue", localStorage, getDiscordWrapper) {

    override val priority: Priority
        get() = Priority.LOW

    override suspend fun configure(data: Any): Any {
        logDebug("configure reaction: $data")
        val command =
            try {
                Json(JsonConfiguration.Stable.copy(isLenient = true)).parse(
                    TodaysGuyCommand.serializer(),
                    data.toString()
                )
            } catch (e: Exception) {
                analytics.logRuleFailed(ruleName, "could not deserialize when configuring keyValue")
                return JsonObject(emptyMap())
            }

        storage.addTriggerForGuild(command.guildId.sf(), command.trigger, command.response)
        return JsonObject(emptyMap())
    }

    override fun getExplanation(): String? {
        return StringBuilder()
            .appendln("See who is currently receiving the spotlight for your server")
            .toString()
    }

    override suspend fun handleEvent(event: RuleBotEvent): Boolean {
        if (event !is MessageCreated) return false

        val guildId = getDiscordWrapper(event)?.getGuildId() ?: return false
        val data = storage.getDataForGuildId(guildId) ?: return false

        if (event.content.startsWith(data.trigger)) {
            getDiscordWrapper(event)?.sendMessage(data.repsonse)
            return true
        }
        return false
    }
}

@Serializable
private data class TodaysGuyCommand(
    val guildId: String,
    val response: String,
    val trigger: String
)