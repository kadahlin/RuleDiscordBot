package com.kyledahlin.myrulebot.app

import arrow.core.flatMap
import arrow.core.right
import com.kyledahlin.rulebot.Analytics
import com.kyledahlin.rulebot.Response
import com.kyledahlin.rulebot.RuleBot
import com.kyledahlin.rulebot.bot.Logger
import io.ktor.application.*
import io.ktor.request.*
import io.ktor.routing.*

fun Routing.rules(analytics: Analytics, rulebot: RuleBot) {
    route("/rules") {
        post("/{ruleName}") {
            val ruleName = call.parameters["ruleName"]!!
            val data = call.receive<String>()
            Logger.logDebug { "got ruleName [$ruleName] with body: [$data]" }
            analytics.logLifecycle("Rule config", "call to configure $ruleName")
            val response = rulebot.configureRule(ruleName, data)
                ?.flatMap { if (it is Unit) EmptyResponse.right() else it.right() }
                ?.fold({ exception ->
                    Response.error(exception.message ?: "no exception message")
                }, { value ->
                    Response.success(value)
                })
                ?: Response.error("No rule found for this name")
            call.jsonResponse(response)
        }

        get {
            val ruleNames = rulebot.getRuleNames()
            println("for rule names got: $ruleNames")
            val data = Response.success(GetRulesResponse(ruleNames.toList()))
            call.jsonResponse(data)
        }
    }

}