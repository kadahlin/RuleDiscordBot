/*
*Copyright 2020 Kyle Dahlin
*
*Licensed under the Apache License, Version 2.0 (the "License");
*you may not use this file except in compliance with the License.
*You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing, software
*distributed under the License is distributed on an "AS IS" BASIS,
*WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*See the License for the specific language governing permissions and
*limitations under the License.
*/
package com.kyledahlin.myrulebot.soundboard

import com.kyledahlin.myrulebot.MyRuleBotScope
import com.kyledahlin.rulebot.bot.*
import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame
import discord4j.voice.AudioProvider
import io.ktor.client.request.get
import java.io.File
import java.nio.ByteBuffer
import javax.inject.Inject

private const val CLIP_COMMAND = "!sound"
private const val PLAY = "play"
private const val SAVE = "save"
private const val AUDIO_DIR = "audio"

/**
 *
 */
@MyRuleBotScope
internal class SoundboardRule @Inject constructor(
    storage: LocalStorage,
    val getDiscordWrapper: GetDiscordWrapperForEvent
) : Rule("Soundboard", storage, getDiscordWrapper) {

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
        AudioSourceManagers.registerRemoteSources(playerManager)
        AudioSourceManagers.registerLocalSource(playerManager)

        // Create an AudioPlayer so Discord4J can receive audio data
        player = playerManager.createPlayer()
        provider = Mp4Player(player)
        scheduler = TrackScheduler(player)
        player.stopTrack()
        player.addListener(object : AudioEventAdapter() {
            override fun onTrackEnd(player: AudioPlayer?, track: AudioTrack?, endReason: AudioTrackEndReason?) {
                logDebug(endReason?.name ?: "unknown end reason")
            }

            override fun onTrackStart(player: AudioPlayer?, track: AudioTrack?) {
                super.onTrackStart(player, track)
                logDebug("track start")
            }
        })
    }

    override suspend fun handleEvent(event: RuleBotEvent): Boolean {
        if (event !is MessageCreated) return false
        val messageContent = event.content
        if (!messageContent.startsWith(CLIP_COMMAND)) {
            return false
        }

        when {
            messageContent == "${CLIP_COMMAND}join"         -> handleJoin(event)
            messageContent.startsWith("$CLIP_COMMAND$PLAY") -> handlePlay(event)
            messageContent.startsWith("$CLIP_COMMAND$SAVE") -> handleSave(event)
            messageContent == "${CLIP_COMMAND}stop"         -> handleStop(event)
        }

        return true
    }

    private suspend fun handleJoin(event: MessageCreated) {
        val wrapper = getDiscordWrapper(event) ?: return

        Logger.logDebug("joining voice")
        wrapper.joinVoiceChannel { setProvider(provider) }
    }

    private suspend fun handlePlay(event: MessageCreated) {
        val isDm = getDiscordWrapper(event)?.isDm == true
        if (!isDm) return
        val messagePieces = event.content.split(" ")
        if (messagePieces.size > 1) {
            val audioFileName = messagePieces[1]
            val filePath = getAudioPathForName(audioFileName)
            Logger.logDebug("trying to play audio: $filePath")
            playerManager.loadItem("$AUDIO_DIR/$filePath", scheduler)
        }
    }

    private suspend fun handleSave(event: MessageCreated) {
        val isDm = getDiscordWrapper(event)?.isDm == true
        if (!isDm) return
        val messagePieces = event.content.split(" ")
        if (messagePieces.size > 1) {
            val fileName = messagePieces[1]
            Logger.logDebug("saving $fileName")
            event.attachments.firstOrNull()?.also {
                val url = it.url
                Logger.logInfo("the url for this attachment is $url")
                val dotIndex = url.lastIndexOf(".")
                val extension = url.substring(dotIndex)
                val bytes = client.get<ByteArray>(it.url)
                File("$AUDIO_DIR/$fileName$extension").outputStream().use { stream -> stream.write(bytes) }
            }
        }
    }

    private suspend fun handleStop(event: MessageCreated) {
        val isDm = getDiscordWrapper(event)?.isDm == true
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