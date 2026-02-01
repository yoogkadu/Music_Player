package com.example.musicplayer.api

import com.example.musicplayer.data.Song
import kotlinx.coroutines.flow.Flow

interface MusicFilesApi {
    fun getSongs(): Flow<List<Song>>

    // This is the trigger called by the ViewModel after permission is granted
    suspend fun refreshSongs()

}
