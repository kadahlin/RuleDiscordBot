package com.kyledahlin.myrulebot.builders

import discord4j.discordjson.json.ApplicationCommandRequest
import discord4k.builders.InteractionApplicationCommandCallbackSpecKt
import strikt.api.Assertion

fun Assertion.Builder<InteractionApplicationCommandCallbackSpecKt>.hasContent(content: String?): Assertion.Builder<InteractionApplicationCommandCallbackSpecKt> =
    assert("checking for content of $content") {
        when (it.content) {
            content -> pass()
            else -> fail(actual = it.content)
        }
    }

fun Assertion.Builder<InteractionApplicationCommandCallbackSpecKt>.hasEmbeddedImage(url: String?): Assertion.Builder<InteractionApplicationCommandCallbackSpecKt> =
    assert("checking for embedded image $url") {
        it.embedSpecs.forEach { spec ->
            if (spec.image == url) {
                pass()
                return@assert
            }
        }
        fail()
    }

fun Assertion.Builder<ApplicationCommandRequest>.isNamed(name: String): Assertion.Builder<ApplicationCommandRequest> =
    assert("Checking for name of $name") {
        when (it.name()) {
            name -> pass()
            else -> fail(actual = it.name())
        }
    }

fun Assertion.Builder<ApplicationCommandRequest>.isTyped(type: Int?): Assertion.Builder<ApplicationCommandRequest> =
    assert("checking for type $type") {
        if (it.type().isAbsent) {
            if (type == null) pass() else fail(actual = "missing")
        } else {
            when (it.type().get()) {
                type -> pass()
                else -> fail(actual = it.type().get())
            }
        }
    }

fun Assertion.Builder<ApplicationCommandRequest>.isSlashCommand() = isTyped(null)
fun Assertion.Builder<ApplicationCommandRequest>.isUserCommand() = isTyped(2)