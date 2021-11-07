package discord4k

import discord4j.common.util.Snowflake
import discord4j.core.DiscordClient
import discord4j.discordjson.json.*
import discord4j.rest.service.ApplicationService

suspend fun DiscordClient.suspendApplicationId() = this.applicationId.suspend()!!

suspend fun DiscordClient.suspendUserInGuild(userId: Snowflake, guildId: Snowflake): UserData =
    this.getMemberById(guildId, userId).user().data.suspend()!!

suspend fun DiscordClient.createMessageInChannel(
    channel: Snowflake,
    onBuild: ImmutableMessageCreateRequest.Builder.() -> Unit
): MessageData? = this.getChannelById(channel)
    .createMessage(MessageCreateRequest.builder().apply(onBuild).build()).suspend()

suspend fun ApplicationService.suspendCreateApplicationCommand(
    applicationId: Long,
    guildId: Long,
    request: ApplicationCommandRequest
) = this.createGuildApplicationCommand(applicationId, guildId, request).suspend()