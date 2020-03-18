/*
*Copyright 2020 Kyle Dahlin
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
package com.kyledahlin.myrulebot

import com.kyledahlin.rulebot.bot.DaggerBotComponent
import com.kyledahlin.rulebot.bot.LogLevel
import com.kyledahlin.rulebot.bot.getStringFromResourceFile

private const val LOG_LEVEL = "--log-level"
private const val IS_BETA = "--beta"
private const val LOG_RULES = "--log-rules"

fun main(args: Array<String>) {
    val metaArgs = parseArgs(args)

    val rulesToLog = metaArgs[LOG_RULES] as? List<String>
    val logLevel = metaArgs[LOG_LEVEL] as? LogLevel ?: LogLevel.INFO

    val isBeta = metaArgs[IS_BETA] as? Boolean
    println("is Beta? $isBeta")

    val tokenFile = if (isBeta == true) "betatoken.txt" else "token.txt"
    val token = getStringFromResourceFile(tokenFile)
    val coreComponent = DaggerBotComponent.builder().setToken(token).build()
    val myRules = DaggerMyRuleBotComponent.builder().botComponent(coreComponent).build().rules()
    val builder = coreComponent.botBuilder().apply {
        addRules(myRules).build().start()
        if (rulesToLog != null) {
            logRules(*rulesToLog.toTypedArray())
        }
        this.logLevel = logLevel
    }
    MyRuleBotStorage.create()
    builder.build().start()
}

//TODO: need a cleaner way to get the command line args
private fun parseArgs(args: Array<String>): Map<String, Any> {
    val result = mutableMapOf<String, Any>()
    val logIndex = args.indexOf(LOG_LEVEL)
    if (logIndex != -1) {
        val logLevel = args[logIndex + 1].toUpperCase()
        result[LOG_LEVEL] = LogLevel.valueOf(logLevel)
    }

    val betaIndex = args.indexOf(IS_BETA)
    if (betaIndex != -1) {
        result[IS_BETA] = true
    }
    val logRules = args.indexOf(LOG_RULES)
    if (logRules != -1) {
        val rulesToLog = args[logRules + 1].toUpperCase()
        result[LOG_RULES] = rulesToLog.split(",")
    }
    return result
}