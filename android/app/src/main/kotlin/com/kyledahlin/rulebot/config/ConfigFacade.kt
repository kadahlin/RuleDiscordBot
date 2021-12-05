package com.kyledahlin.rulebot.config

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import com.kyledahlin.models.GetGuildsResponse
import com.kyledahlin.models.GetRulesResponse
import com.kyledahlin.models.MemberNameAndIds
import com.kyledahlin.models.Response
import javax.inject.Inject

class ConfigFacade @Inject constructor(private val service: ConfigService) {
    suspend fun getRules(): Either<String, GetRulesResponse> = Either.catch({ throwable ->
        "Unable to reach the service to load rules, ${throwable.message}"
    }, {
        service.getRules()
    }).flatMap { response ->
        when (response) {
            is Response.Failure -> response.reason.left()
            is Response.Success -> (response.data as GetRulesResponse).right()
        }
    }

    suspend fun getGuilds(): Either<String, GetGuildsResponse> = Either.catch({ throwable ->
        "Unable to reach the service for guilds, ${throwable.message}"
    }, {
        service.getGuilds()
    }).flatMap { response ->
        when (response) {
            is Response.Failure -> response.reason.left()
            is Response.Success -> (response.data as GetGuildsResponse).right()
        }
    }

    suspend fun getGuildInfo(guildId: String): Either<String, MemberNameAndIds> = Either.catch({ throwable ->
        "Unable to reach the service for guilds, ${throwable.message}"
    }, {
        service.getGuildInfo(guildId)
    }).flatMap { response ->
        when (response) {
            is Response.Failure -> response.reason.left()
            is Response.Success -> (response.data as MemberNameAndIds).right()
        }
    }
}