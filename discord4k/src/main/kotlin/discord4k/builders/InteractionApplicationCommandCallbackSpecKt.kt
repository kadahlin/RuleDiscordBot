package discord4k.builders

import discord4j.core.`object`.component.LayoutComponent
import discord4j.core.spec.EmbedCreateSpec
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec
import discord4j.core.spec.InteractionReplyEditSpec

open class InteractionApplicationCommandCallbackSpecKt {
    var content: String? = null
    var embedSpecs = mutableListOf<EmbedCreateSpecKt>()
    var withEphemeral: Boolean = false
    val components = mutableListOf<LayoutComponent>()

    fun withEphemeral() {
        withEphemeral = true
    }

    fun content(c: () -> String) {
        content = c()
    }

    fun addEmbed(build: EmbedCreateSpecKt.() -> Unit) {
        embedSpecs.add(EmbedCreateSpecKt().apply(build))
    }

    fun addComponent(build: () -> LayoutComponent) {
        components.add(build())
    }
}

class EmbedCreateSpecKt {
    var image: String? = null

    fun image(i: () -> String) {
        image = i()
    }
}

fun interactionApplicationCommandCallbackSpecKt(build: InteractionApplicationCommandCallbackSpecKt.() -> Unit) =
    InteractionApplicationCommandCallbackSpecKt().apply(build).run {
        val builder = InteractionApplicationCommandCallbackSpec.builder()
        builder.ephemeral(withEphemeral)
        content?.let { builder.content(it) }
        embedSpecs.forEach { spec ->
            builder.addEmbed(
                EmbedCreateSpec.builder().apply {
                    spec.image?.let { image(it) }
                }.build()
            )
        }
        if (components.isEmpty()) {
            builder.components(emptyList())
        } else {
            components.forEach {
                builder.addComponent(it)
            }
        }
        builder.build()
    }

class InteractionReplyEditSpecKt {
    var content: String? = null
    var embedSpecs = mutableListOf<EmbedCreateSpecKt>()
    var withEphemeral: Boolean = false
    val components = mutableListOf<LayoutComponent>()

    fun content(c: () -> String) {
        content = c()
    }

    fun addEmbed(build: EmbedCreateSpecKt.() -> Unit) {
        embedSpecs.add(EmbedCreateSpecKt().apply(build))
    }

    fun addComponent(build: () -> LayoutComponent) {
        components.add(build())
    }
}

fun interactionReplyEditSpecKt(build: InteractionReplyEditSpecKt.() -> Unit): InteractionReplyEditSpec =
    InteractionReplyEditSpecKt().apply(build).run {
        val builder = InteractionReplyEditSpec.builder()
        content?.let { builder.contentOrNull(it) }
        embedSpecs.forEach { spec ->
            builder.addEmbed(
                EmbedCreateSpec.builder().apply {
                    spec.image?.let { image(it) }
                }.build()
            )
        }
        if (components.isEmpty()) {
            builder.components(emptyList())
        } else {
            components.forEach {
                builder.addComponent(it)
            }
        }
        builder.build()
    }
