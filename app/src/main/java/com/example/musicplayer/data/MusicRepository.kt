package com.example.musicplayer.data

import kotlinx.coroutines.flow.Flow

interface MusicRepository {
    fun observeSongs(): Flow<List<Song>>
    suspend fun refreshSongs()

}
