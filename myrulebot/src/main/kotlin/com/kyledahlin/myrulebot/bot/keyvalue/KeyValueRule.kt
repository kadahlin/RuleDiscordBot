package com.kyledahlin.myrulebot.bot.keyvalue

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.kyledahlin.myrulebot.bot.MyRuleBotScope
import com.kyledahlin.rulebot.Analytics
import com.kyledahlin.rulebot.bot.GetDiscordWrapperForEvent
import com.kyledahlin.rulebot.bot.MessageCreated
import com.kyledahlin.rulebot.bot.Rule
import com.kyledahlin.rulebot.bot.RuleBotEvent
import com.kyledahlin.rulebot.sf
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject

@MyRuleBotScope
internal class KeyValueRule @Inject constructor(
    private val storage: KeyValueRuleStorage,
    private val getDiscordWrapper: GetDiscordWrapperForEvent,
    private val analytics: Analytics
) :
    Rule("KeyValue", getDiscordWrapper) {

    override val priority: Priority
        get() = Priority.LOW

    override suspend fun configure(data: Any): Either<Exception, Any> {
        logDebug("configuring reaction rule: $data")
        val command =
            try {
                Json { isLenient = true }.decodeFromString(
                    KeyValueCommand.serializer(),
                    data.toString()
                )
            } catch (e: Exception) {
                analytics.logRuleFailed(ruleName, "could not deserialize when configuring keyValue")
                return Exception("failed to deserialize command given to this rule").left()
            }

        storage.addTriggerForGuild(command.guildId.sf(), command.trigger, command.response)
        return emptyMap<String, String>().right()
    }

    override fun getExplanation(): String? {
        return StringBuilder()
            .appendLine("Pinned messages basically")
            .toString()
    }

    override suspend fun handleEvent(event: RuleBotEvent): Boolean {
        if (event !is MessageCreated) return false

        val guildId = getDiscordWrapper(event)?.getGuildId() ?: return false
        val data = storage.getDataForGuildId(guildId)

        data.firstOrNull { it.trigger == event.content }?.let {
            getDiscordWrapper(event)?.sendMessage(it.response)
            return true
        }
        return false
    }
}

@Serializable
private data class KeyValueCommand(
    val guildId: String,
    val trigger: String,
    val response: String,
)