package com.kyledahlin.myrulebot.bot

import com.google.api.core.ApiFuture
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

suspend inline fun <T> ApiFuture<T>.suspend() = suspendCoroutine<T> { cont ->
    addListener({
        cont.resume(get())
    }, Executors.newFixedThreadPool(2))
}