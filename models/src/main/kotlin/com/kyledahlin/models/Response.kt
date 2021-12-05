package com.kyledahlin.models

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable

/**
 * Object that will be sent back to clients calling the configure API. The object will contain either the data object
 * or the error object, but never both
 */
@Serializable
sealed class Response {

    @Serializable
    class Failure(val reason: String = "") : Response()

    @Serializable
    class Success(val data: @Polymorphic Any) : Response()

    companion object {
        fun error(reason: String = ""): Response = Failure(reason = reason)

        fun success(data: Any): Response = Success(data = data)
    }
}