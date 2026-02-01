package com.example.musicplayer.data

import com.example.musicplayer.api.MusicFilesApi
import kotlinx.coroutines.flow.Flow

class MainMusicRepository(private val musicFilesApi: MusicFilesApi,
    private val internetApi:String="") : MusicRepository {
    override fun observeSongs(): Flow<List<Song>> = musicFilesApi.getSongs()

    // Triggering the API's load function
    override suspend fun refreshSongs() {
        musicFilesApi.refreshSongs()
    }
}