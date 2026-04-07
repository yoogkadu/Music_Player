package com.example.musicplayer.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.IGNORE
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import com.example.musicplayer.data.Song
import com.example.musicplayer.database.table.SongEntity
import kotlinx.coroutines.flow.Flow
import java.net.URI
import kotlin.time.Clock

@Dao
interface SongEntityDao {

    @Query("SELECT * FROM songs ORDER BY dateAdded DESC")
    fun getAllSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE songId = :songId LIMIT 1")
    fun getSongByHash(songId: String): SongEntity?

    @Query("SELECT * FROM songs where mediaStoreId = :id LIMIT 1")
    fun getSongByMediaStore(id: Long): SongEntity?

    @Upsert
    suspend fun  upsertSongs(songs: List<SongEntity>)

    @Upsert
    suspend fun upsertSong(song : SongEntity)

    @Delete
    suspend fun deleteSong(song : SongEntity)

    @Insert(onConflict = IGNORE)
    suspend fun insertALlBunch(song : List<SongEntity>) : List<Long>

    @Update
    suspend fun updateExisting(song : SongEntity)

    @Query("""
        Update songs set title = :title, duration = :duration,
        artist = :artist,
        album = :album,
        mediaStoreId = :mediaStoreId,
        albumArtist = :albumArtist,
        dateModified = :dateModified
        where songId = :songId
    """)
    suspend fun updateSongKeepDate(
        songId: String,
        title: String,
        duration: Long,
        artist: String,
        album: String,
        dateModified : Long,
        mediaStoreId : Long,
        albumArtist : String
    )

    @Transaction
    suspend fun smartUpsert(songs : List<Song>)
    {
       val songEntities = songs.map {
           SongEntity(
               songId = it.stableId,
               title = it.title,
               duration = it.duration,
               mediaStoreId = it.id.toLong(),
               artist = it.artist,
               album = it.album,
               albumArtist = it.albumArtist,
               isFavorite = false,
               dateAdded = System.currentTimeMillis(),
               dateModified = System.currentTimeMillis(),
           )
       }
        val insertResult = insertALlBunch(songEntities)
        val existingSongs= songEntities.filterIndexed { index, song ->
            insertResult[index] == -1L
        }
        if(existingSongs.isNotEmpty()){
            updateExistingMetadataOnly(existingSongs)
        }
    }
    @Transaction
    suspend fun updateExistingMetadataOnly(songs: List<SongEntity>) {
        songs.forEach { song ->
            updateSingleMetadata(
                song.songId, song.title, song.duration,
                song.artist, song.album, song.mediaStoreId
            )
        }
    }
    @Query("""
        UPDATE songs SET 
            title = :title, duration = :duration, 
            artist = :artist, album = :album, 
            mediaStoreId = :mediaStoreId 
        WHERE songId = :songId
    """)
    suspend fun updateSingleMetadata(
        songId: String, title: String, duration: Long,
        artist: String, album: String, mediaStoreId: Long
    )
}