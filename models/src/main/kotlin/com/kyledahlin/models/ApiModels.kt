package com.kyledahlin.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@Serializable
data class GetRulesResponse(val rules: List<String>)

@Serializable
data class GetGuildsResponse(val guilds: List<GuildNameAndId>)

@Serializable
data class MemberNameAndIds(val members: List<MemberNameAndId>)

@Serializable
data class GuildNameAndId(
    val name: String,
    val id: String
)

@Serializable
data class MemberNameAndId(
    val name: String,
    val id: String
)

@Serializable
object EmptyResponse

val basicResponseModule = SerializersModule {
    polymorphic(Any::class) {
        subclass(GetRulesResponse::class)
        subclass(GetGuildsResponse::class)
        subclass(MemberNameAndIds::class)
        subclass(EmptyResponse::class)
    }
}