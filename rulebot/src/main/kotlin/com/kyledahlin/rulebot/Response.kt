package com.kyledahlin.rulebot

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable

/**
 * Object that will be sent back to clients calling the configure API. The object will contain either the data object
 * or the error object, but never both
 */
@Serializable
class Response {

    @Polymorphic
    val data: Any?
    val error: Error?

    constructor(data: Any) {
        this.data = data
        this.error = null
    }

    constructor(error: Error) {
        this.error = error
        this.data = null
    }

    @Serializable
    class Error(val code: Int = 0, val reason: String? = null)

    companion object {
        val success: Response
            get() = Response(data = "success")
    }
}