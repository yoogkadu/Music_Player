package com.example.musicplayer.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.IGNORE
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.example.musicplayer.database.table.PlaylistEntity
import com.example.musicplayer.database.table.PlaylistSongCrossRef
import com.example.musicplayer.database.table.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao{

    @Upsert()
    suspend fun insertPlaylist(playlistEntity: PlaylistEntity) : Long

    @Insert(onConflict = IGNORE)
    suspend fun addSongToPlaylist(crossRef: PlaylistSongCrossRef)

    @Query("SELECT * FROM PLAYLISTS ORDER BY createdAt DESC")
    fun getALlPlaylistFlow() : Flow<List<PlaylistEntity>>

    @Query("""
        SELECT * FROM songs s
        INNER JOIN playlist_song_cross_ref r ON s.hash = r.songHash
        Where r.playlistId = :playlistId
        ORDER BY r.sequenceOrder ASC
    """)
    fun getSongInPlayListFlow(playlistId : Long) : Flow<List<SongEntity>>

    @Transaction
    suspend fun safeAddSongToPlaylist(playlistEntity: PlaylistEntity,songHash : String){
        val playlistId = insertPlaylist(playlistEntity)
        addSongToPlaylist(PlaylistSongCrossRef(playlistId,songHash,0))
    }
}