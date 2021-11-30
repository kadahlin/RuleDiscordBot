package com.kyledahlin.wellness.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@Serializable
data class WellnessResponse(val message: String)

val wellnessSerializerModule = SerializersModule {
    polymorphic(Any::class) {
        subclass(WellnessResponse::class)
    }
}