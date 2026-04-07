package com.example.musicplayer.database

import com.example.musicplayer.data.Song
import com.example.musicplayer.database.dao.PlaylistSongInfo
import com.example.musicplayer.database.table.PlaylistEntity
import com.example.musicplayer.database.table.PlaylistSongCrossRef
import com.example.musicplayer.database.table.SongEntity
import kotlinx.coroutines.flow.Flow

class AndroidDataBaseInterface(private val database: MusicDatabase) : DataBaseInterface{
    override fun getPlaylist(): Flow<List<PlaylistEntity>> {
        return database.playlistDao().getALlPlaylistFlow()
    }

    override  fun getSongsFromPlaylistId(playlistId: Int): Flow<List<SongEntity>> {
         return database.playlistDao().getSongInPlayListFlow(playlistId.toLong())
    }

    override suspend fun createPlaylistAndAddSongs(
        playlistName : String,
        songs : List<Song>
    ) {
        database.playlistDao().createPlaylistAndAddSongs(playlistName = playlistName, songs = songs)
    }


    override  fun getAllPlaylistSongs(): Flow<List<PlaylistSongInfo>> {
        return database.playlistDao().getAllPlaylistSongs()
    }

    override suspend fun addOrUpdateSongs(songs: List<Song>) {
        database.songEntityDao().smartUpsert(songs)
    }

}