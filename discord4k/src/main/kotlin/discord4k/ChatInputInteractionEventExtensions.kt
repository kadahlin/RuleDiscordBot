package discord4k

import discord4j.core.event.domain.interaction.InteractionCreateEvent
import discord4k.builders.InteractionApplicationCommandCallbackSpecKt
import discord4k.builders.InteractionReplyEditSpecKt
import discord4k.builders.interactionApplicationCommandCallbackSpecKt
import discord4k.builders.interactionReplyEditSpecKt
import suspend

suspend fun InteractionCreateEvent.suspendReply(spec: InteractionApplicationCommandCallbackSpecKt.() -> Unit) =
    this.reply(interactionApplicationCommandCallbackSpecKt(spec)).suspend()

suspend fun InteractionCreateEvent.suspendEditReply(spec: InteractionReplyEditSpecKt.() -> Unit) =
    this.editReply(interactionReplyEditSpecKt(spec)).suspend()