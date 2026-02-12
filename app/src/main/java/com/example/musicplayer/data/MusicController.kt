package com.example.musicplayer.data

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaBrowser
import androidx.media3.session.SessionToken
import com.example.musicplayer.services.PlaybackService
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MusicController(context : Context) {
    private var browser : MediaBrowser? = null

    private val _player = MutableStateFlow<Player?>(null)
    val player = _player.asStateFlow()
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    init {
        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val controllerFuture = MediaBrowser.Builder(context, sessionToken).buildAsync()

        controllerFuture.addListener(
            {
                browser = controllerFuture.get()
                _player.value=browser
                browser?.addListener(object : Player.Listener{
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        super.onIsPlayingChanged(isPlaying)
                        _isPlaying.value=isPlaying
                    }

                    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                        super.onMediaItemTransition(mediaItem, reason)

                    }

                    override fun onDeviceVolumeChanged(volume: Int, muted: Boolean) {
                        super.onDeviceVolumeChanged(volume, muted)

                    }

                })
            },
            MoreExecutors.directExecutor()
        )
    }

    fun play(song: Song){
        val mediaItem = MediaItem.Builder()
            .setMediaId(song.id)
            .setUri(song.uri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(song.title)
                    .setArtist(song.artist)
                    .setAlbumArtist(song.albumArtist)
                    .setArtworkUri(song.albumArtUri)
                    .build()
            ).build()
        browser?.setMediaItem(mediaItem)
        browser?.prepare()
        browser?.play()
    }

    fun release(){
        browser?.run {
            stop()       // Stop the music first
            release()    // Then release the hardware
        }
        browser = null
    }
}