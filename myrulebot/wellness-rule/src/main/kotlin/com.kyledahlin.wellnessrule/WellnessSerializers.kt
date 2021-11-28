package com.kyledahlin.wellnessrule

import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

val wellnessSerializers = SerializersModule {
    polymorphic(Any::class) {
        subclass(WellnessResponse::class)
    }
}