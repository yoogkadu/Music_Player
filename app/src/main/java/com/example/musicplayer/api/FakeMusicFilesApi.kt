package com.example.musicplayer.api

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import com.example.musicplayer.data.Song
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeMusicFilesApi: MusicFilesApi {
    private val _songFlow = MutableStateFlow<List<Song>>(emptyList())
    override fun getSongs(): Flow<List<Song>> = _songFlow.asStateFlow()
    override suspend fun refreshSongs() {
        Log.d("MusicDebug", "Starting refresh...")
        delay(500)
        val songList= listOf(
            Song(
                id = "1",
                title = "Test Song",
                artist = "Unknown",
                duration = 180_000,
                uri = Uri.EMPTY,
                albumArtUri = Uri.EMPTY,
                album = "Unknown"
            ),
            Song(
                id = "2",
                title = "Another Track",
                artist = "Demo Artist",
                duration = 210_000,
                uri = Uri.EMPTY,
                albumArtUri = "https://picsum.photos/200".toUri(),
                album = "Unknown"
            )
        )
        _songFlow.value = songList
        Log.d("MusicDebug", "Refresh complete! Songs sent: ${songList.size}")
    }

}