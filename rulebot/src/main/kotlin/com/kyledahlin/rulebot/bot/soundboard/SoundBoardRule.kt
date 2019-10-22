package com.kyledahlin.rulebot.bot.soundboard

import com.kyledahlin.rulebot.bot.LocalStorage
import com.kyledahlin.rulebot.bot.Logger
import com.kyledahlin.rulebot.bot.Rule
import com.kyledahlin.rulebot.bot.client
import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame
import discord4j.core.event.domain.message.MessageCreateEvent
import discord4j.voice.AudioProvider
import io.ktor.client.request.get
import kotlinx.coroutines.reactive.awaitFirstOrNull
import java.io.File
import java.nio.ByteBuffer
import javax.inject.Inject

private const val CLIP_COMMAND = "!sound"
private const val PLAY = "play"
private const val SAVE = "save"
private const val AUDIO_DIR = "audio"

internal class SoundboardRule @Inject constructor(storage: LocalStorage) : Rule("Soundboard", storage) {

    override val priority: Priority
        get() = Priority.NORMAL

    // Creates AudioPlayer instances and translates URLs to AudioTrack instances
    private val playerManager = DefaultAudioPlayerManager()
    private var provider: Mp4Player
    private var scheduler: TrackScheduler
    private var player: AudioPlayer

    init {
        val audioDir = File(AUDIO_DIR)
        if (!audioDir.exists()) {
            audioDir.mkdir()
        }
        // Allow playerManager to parse remote sources like YouTube links
        AudioSourceManagers.registerRemoteSources(playerManager)    //TODO: needed?
        AudioSourceManagers.registerLocalSource(playerManager)

        // Create an AudioPlayer so Discord4J can receive audio data
        player = playerManager.createPlayer()
        provider = Mp4Player(player)
        scheduler = TrackScheduler(player)
        player.stopTrack()
    }

    override suspend fun handleRule(messageEvent: MessageCreateEvent): Boolean {
        val messageContent = messageEvent.message.content.get()
        if (!messageContent.startsWith(CLIP_COMMAND)) {
            return false
        }

        when {
            messageContent == "${CLIP_COMMAND}join" -> handleJoin(messageEvent)
            messageContent.startsWith("${CLIP_COMMAND}$PLAY") -> handlePlay(messageEvent)
            messageContent.startsWith("${CLIP_COMMAND}$SAVE") -> handleSave(messageEvent)
            messageContent == "${CLIP_COMMAND}stop" -> handleStop(messageEvent)
        }

        return true
    }

    private suspend fun handleJoin(event: MessageCreateEvent) {
        val isDm = !event.member.isPresent
        if (isDm) return
        Logger.logDebug("joining voice")
        val member = event.member.get()
        val voiceState = member.voiceState.awaitFirstOrNull()
        val channel = voiceState?.channel?.awaitFirstOrNull()
        channel?.join { voiceChannelSpec ->
            voiceChannelSpec.setProvider(provider)
        }?.subscribe()
    }

    private suspend fun handlePlay(event: MessageCreateEvent) {
        val isDm = event.guild.awaitFirstOrNull() == null
        if (!isDm) return
        val messagePieces = event.message.content.get().split(" ")
        if (messagePieces.size > 1) {
            val audioFileName = messagePieces[1]
            val filePath = getAudioPathForName(audioFileName)
            Logger.logDebug("trying to play audio: $filePath")
            playerManager.loadItem("$AUDIO_DIR/$filePath", scheduler)
        }
    }

    private suspend fun handleSave(event: MessageCreateEvent) {
        val isDm = event.guild.awaitFirstOrNull() == null
        if (!isDm) return
        val messagePieces = event.message.content.get().split(" ")
        if (messagePieces.size > 1) {
            val fileName = messagePieces[1]
            Logger.logDebug("saving $fileName")
            event.message.attachments.firstOrNull()?.also {
                val url = it.url
                Logger.logInfo("the url was this attachment is $url")
                val dotIndex = url.lastIndexOf(".")
                val extension = url.substring(dotIndex)
                val bytes = client.get<ByteArray>(it.url)
                File("$AUDIO_DIR/$fileName$extension").outputStream().use { stream -> stream.write(bytes) }
            }
        }
    }

    private suspend fun handleStop(event: MessageCreateEvent) {
        val isDm = event.guild.awaitFirstOrNull() == null
        if (!isDm) return
        player.stopTrack()
    }

    private fun getAudioPathForName(name: String): String? {
        return File(AUDIO_DIR)
            .listFiles()
            .firstOrNull {
                it.name.startsWith(name)
            }?.name
    }

    override fun getExplanation(): String? {
        return StringBuilder().apply {
            appendln("Add new sound clips to be played")
        }.toString()
    }

}

private class Mp4Player(val player: AudioPlayer) :
    AudioProvider(ByteBuffer.allocate(StandardAudioDataFormats.DISCORD_OPUS.maximumChunkSize())) {

    private val frame = MutableAudioFrame()

    init {
        frame.setBuffer(buffer)
    }

    override fun provide(): Boolean {
        val didProvide = player.provide(frame)
        // If audio was provided, flip from write-mode to read-mode
        if (didProvide) {
            buffer.flip()
        }
        return didProvide
    }

}

class TrackScheduler(private val player: AudioPlayer) : AudioLoadResultHandler {
    override fun loadFailed(exception: FriendlyException?) {
        Logger.logDebug("load failed $exception")
    }

    override fun trackLoaded(track: AudioTrack?) {
        Logger.logDebug("loaded successfully")
        player.playTrack(track)
    }

    override fun noMatches() {
        Logger.logDebug("no matches")
    }

    override fun playlistLoaded(playlist: AudioPlaylist?) {
        Logger.logDebug("playlist loaded")
    }
}