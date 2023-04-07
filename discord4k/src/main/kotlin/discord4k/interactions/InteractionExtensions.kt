package discord4k.interactions

import discord4j.core.`object`.command.Interaction
import discord4j.core.`object`.entity.Guild
import discord4k.suspend

suspend fun Interaction.suspendGuild(): Guild = guild.suspend()!!