package com.kyledahlin.rulebot.bot.java

import com.kyledahlin.rulebot.bot.Rule
import com.kyledahlin.rulebot.bot.RuleBotEvent

/**
 * Middle man to allow a java class to be used as a [Rule]
 */
abstract class JavaRule(protected val localStorage: JavaLocalStorage, protected val eventStorage: JavaEventStorage, ruleName: String) :
    Rule(ruleName, localStorage, eventStorage) {

    abstract fun blockHandleEvent(event: RuleBotEvent): Boolean

    abstract fun blockConfigure(data: Any): Any

    override suspend fun handleEvent(event: RuleBotEvent): Boolean {
        return blockHandleEvent(event)
    }

    override suspend fun configure(data: Any): Any {
        return blockConfigure(data)
    }
}