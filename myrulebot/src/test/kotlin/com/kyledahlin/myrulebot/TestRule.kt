package com.kyledahlin.myrulebot

import arrow.core.Either
import arrow.core.right
import com.kyledahlin.rulebot.bot.Rule
import kotlinx.serialization.Serializable

class TestRule : Rule("testRule") {

    var data: Any? = null

    override fun handlesCommand(name: String): Boolean {
        return name == ruleName
    }

    override suspend fun configure(data: Any): Either<Exception, Any> {
        this.data = data
        return TestResponse(data = "configured").right()
    }
}

@Serializable
data class TestResponse(val data: String)