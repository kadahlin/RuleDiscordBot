package discord4k.interactions

import discord4j.core.`object`.entity.User
import discord4j.core.event.domain.interaction.UserInteractionEvent
import discord4k.suspend

suspend fun UserInteractionEvent.suspendTargetUser(): User = targetUser.suspend()!!