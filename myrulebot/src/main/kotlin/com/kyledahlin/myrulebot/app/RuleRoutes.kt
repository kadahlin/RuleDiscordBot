package com.kyledahlin.myrulebot.app

import com.kyledahlin.models.GetRulesResponse
import com.kyledahlin.rulebot.Analytics
import com.kyledahlin.rulebot.Response
import com.kyledahlin.rulebot.RuleBot
import com.kyledahlin.rulebot.bot.Logger.logDebug
import io.ktor.application.*
import io.ktor.request.*
import io.ktor.routing.*

fun Routing.rules(analytics: Analytics, rulebot: RuleBot) {
    route("/rules") {
        post("/{ruleName}") {
            val ruleName = call.parameters["ruleName"]!!
            val data = call.receive<String>()
            logDebug { "got ruleName [$ruleName] with body: [$data]" }
            analytics.logLifecycle("Rule config", "call to configure $ruleName")
            val response = rulebot.configureRule(ruleName, data)
                ?.fold({ exception ->
                    Response.error(exception.toString())
                }, { value ->
                    Response.success(value)
                })
                ?: Response.error("No rule found for name $ruleName")
            call.jsonResponse(response)
        }

        get {
            val ruleNames = rulebot.getRuleNames()
            logDebug { "for rule names got: $ruleNames" }
            val data = Response.success(GetRulesResponse(ruleNames.toList()))
            call.jsonResponse(data)
        }
    }
}