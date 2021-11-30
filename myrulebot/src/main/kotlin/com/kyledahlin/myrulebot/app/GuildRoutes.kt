package com.kyledahlin.myrulebot.app

import com.kyledahlin.models.GetGuildsResponse
import com.kyledahlin.models.MemberNameAndIds
import com.kyledahlin.rulebot.Response
import com.kyledahlin.rulebot.RuleBot
import com.kyledahlin.rulebot.bot.Logger
import io.ktor.application.*
import io.ktor.routing.*

fun Routing.guilds(ruleBot: RuleBot) {
    route("/guilds") {
        get {
            val guildInfo = ruleBot.getGuildInfo()
            Logger.logDebug { "Returning guild info, $guildInfo" }
            call.jsonResponse(Response.success(GetGuildsResponse(guildInfo)))
        }

        get("/{guildId}") {
            val list = ruleBot.getMemberInfo(call.parameters["guildId"]!!)?.toList() ?: emptyList()
            call.jsonResponse(Response.success(MemberNameAndIds(list)))
        }
    }
}