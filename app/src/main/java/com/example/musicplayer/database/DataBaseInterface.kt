package com.example.musicplayer.database

import com.example.musicplayer.data.Song
import com.example.musicplayer.database.table.PlaylistEntity
import com.example.musicplayer.database.table.SongEntity
import kotlinx.coroutines.flow.Flow

interface DataBaseInterface{
    fun getPlaylist() : Flow<List<PlaylistEntity>>
    fun getSongsFromPlaylistId(playlistId : Int) : Flow<List<SongEntity>>

    suspend fun createPlaylist(playlistEntity: PlaylistEntity,songs : List<SongEntity>) : Unit
}