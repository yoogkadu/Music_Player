package com.example.musicplayer.database

import com.example.musicplayer.data.Song
import com.example.musicplayer.database.table.PlaylistEntity
import com.example.musicplayer.database.table.PlaylistSongCrossRef
import com.example.musicplayer.database.table.SongEntity
import kotlinx.coroutines.flow.Flow

class AndroidDataBaseInterface(private val database: MusicDatabase) : DataBaseInterface{
    override fun getPlaylist(): Flow<List<PlaylistEntity>> {
        return database.playlistDao().getALlPlaylistFlow()
    }

    override  fun getSongsFromPlaylistId(playlistId: Int): Flow<List<SongEntity>> {
         return database.playlistDao().getSongInPlayListFlow(playlistId)
    }

    override suspend fun createPlaylist(
        playlistEntity: PlaylistEntity,
        songs: List<SongEntity>
    ) {
        database.playlistDao().insertPlaylist(playlistEntity)
        songs.forEach {
            song ->
            database.playlistDao().safeAddSongToPlaylist(
                PlaylistEntity(
                    playlistId = playlistEntity.playlistId,
                    name = playlistEntity.name,
                    createdAt = playlistEntity.createdAt
                ),
                songHash = song.hash
            )
        }
    }


}