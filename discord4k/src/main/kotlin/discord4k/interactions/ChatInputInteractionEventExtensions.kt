package discord4k.interactions

import discord4j.core.event.domain.interaction.ComponentInteractionEvent
import discord4j.core.event.domain.interaction.InteractionCreateEvent
import discord4k.builders.InteractionApplicationCommandCallbackSpecKt
import discord4k.builders.InteractionReplyEditSpecKt
import discord4k.builders.interactionApplicationCommandCallbackSpecKt
import discord4k.builders.interactionReplyEditSpecKt
import discord4k.suspend

suspend fun InteractionCreateEvent.suspendReply(spec: InteractionApplicationCommandCallbackSpecKt.() -> Unit) =
    this.reply(interactionApplicationCommandCallbackSpecKt(spec)).suspend()

suspend fun InteractionCreateEvent.suspendDeferReply() {
    deferReply().suspend()
}

suspend fun InteractionCreateEvent.suspendEditReply(spec: InteractionReplyEditSpecKt.() -> Unit) =
    this.editReply(interactionReplyEditSpecKt(spec)).suspend()

suspend fun ComponentInteractionEvent.suspendEdit(spec: InteractionApplicationCommandCallbackSpecKt.() -> Unit) =
    this.edit(interactionApplicationCommandCallbackSpecKt(spec)).suspend()