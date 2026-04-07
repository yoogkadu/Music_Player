package com.example.musicplayer.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.IGNORE
import androidx.room.Query

import androidx.room.Transaction
import androidx.room.Upsert
import com.example.musicplayer.data.Song
import com.example.musicplayer.database.table.PlaylistEntity
import com.example.musicplayer.database.table.PlaylistSongCrossRef
import com.example.musicplayer.database.table.SongEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect

@Dao
interface PlaylistDao{
    @Upsert()
    suspend fun insertPlaylist(playlistEntity: PlaylistEntity) : Long



//    @Query("Select name from playlist")
//    suspend fun getPlaylistByName(name : String)
    @Insert(onConflict = IGNORE)
    suspend fun addSongToPlaylist(crossRef: PlaylistSongCrossRef)

    @Insert(onConflict = IGNORE)
    suspend fun addSongListToPlaylist(crossref : List<PlaylistSongCrossRef>)

    @Query("SELECT * FROM  playlists ORDER BY  createdAt DESC")
    fun getALlPlaylistFlow() : Flow<List<PlaylistEntity>>
    @Query("""
        SELECT s.* FROM songs s
        INNER JOIN playlist_song_cross_ref r ON s.songId = r.songId
        and r.playlistId = :playlistId
        ORDER BY r.sequenceOrder ASC
    """)
    fun getSongInPlayListFlow(playlistId : Long) : Flow<List<SongEntity>>

    @Transaction
    suspend fun createPlaylistAndAddSongs(playlistName : String,
                                          songs : List<Song>) {

        val playlistId = insertPlaylist(PlaylistEntity(name = playlistName))
        if(playlistId ==- 1L) return
        addSongListToPlaylist(songs.mapIndexed{
            index,song ->
            PlaylistSongCrossRef(playlistId,song.stableId,index.toLong())
        })

    }

    @Query("""
        Select name as playlistName, playlists.playlistId as playlistId, songId,sequenceOrder,createdAt from playlist_song_cross_ref,playlists
        where playlist_song_cross_ref.playlistId = playlists.playlistId order by sequenceOrder asc
    """)
     fun getAllPlaylistSongs() : Flow<List<PlaylistSongInfo>>

}
data class PlaylistSongInfo(
    val playlistId: Long,
    val playlistName: String,
    val songId: String,
    val sequenceOrder: Long,
    val createdAt : Long
)