package discord4k

import com.kyledahlin.models.Username
import discord4j.common.util.Snowflake
import discord4j.core.DiscordClient
import discord4j.core.`object`.VoiceState
import discord4j.core.`object`.entity.Guild
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

suspend fun DiscordClient.getUsersInSameVoiceChannel(guild: Guild, voiceState: VoiceState): List<Username> {
    val voiceChannelId = voiceState.suspendChannel()!!.id
    val voiceStates = guild.voiceStates.collectList().suspend()!!
    return voiceStates.filter { vs -> vs.channelId.isPresent && vs.channelId.get() == voiceChannelId }
        .map { Username(it.suspendMember()!!.displayName) }
}