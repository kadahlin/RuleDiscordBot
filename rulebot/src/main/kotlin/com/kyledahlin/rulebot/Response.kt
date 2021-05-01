package com.kyledahlin.rulebot

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable

/**
 * Object that will be sent back to clients calling the configure API. The object will contain either the data object
 * or the error object, but never both
 */
@Serializable
class Response(@Polymorphic val data: Any? = null, val error: Error = Error()) {

    @Serializable
    class Error(val reason: String = "")

    companion object {
        fun error(reason: String = ""): Response {
            return Response(error = Error(reason))
        }

        fun success(data: Any): Response {
            return Response(data = data)
        }

        fun success(): Response = Response(data = emptyMap<String, String>())
    }
}