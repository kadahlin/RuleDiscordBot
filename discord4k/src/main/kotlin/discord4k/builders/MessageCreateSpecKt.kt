package discord4k.builders

import discord4j.core.`object`.component.LayoutComponent
import discord4j.core.spec.MessageCreateSpec

class MessageCreateSpecKt {
    var content: String? = null
    var components = mutableListOf<LayoutComponent>()

    fun content(c: () -> String) {
        this.content = c()
    }

    fun addComponent(component: () -> LayoutComponent) {
        components.add(component())
    }
}

fun messageCreateSpecKt(onBuild: MessageCreateSpecKt.() -> Unit) =
    MessageCreateSpecKt().apply(onBuild).run {
        val builder = MessageCreateSpec.builder()
        if (content != null) {
            builder.content(content!!)
        }
        components.forEach(builder::addComponent)

        builder.build()
    }
