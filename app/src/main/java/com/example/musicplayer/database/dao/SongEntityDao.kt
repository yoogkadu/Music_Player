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
    suspend fun getSongByHash(songId: String): SongEntity?
    @Query("SELECT * FROM songs where mediaStoreId = :id LIMIT 1")
    suspend fun getSongByMediaStore(id: Long): SongEntity?
    @Upsert
    suspend fun  upsertSongs(songs: List<SongEntity>)
    @Upsert
    suspend fun upsertSong(song : SongEntity)
    @Delete
    suspend fun deleteSong(song : SongEntity)
    @Insert(onConflict = IGNORE)
    suspend fun insertAllBunch(song : List<SongEntity>) : List<Long>
    @Update
    suspend fun updateExisting(song : SongEntity)

    @Query("""
        Update songs set title = :title,
        duration = :duration,
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
    suspend fun smartUpsert(songs: List<Song>) {
        if (songs.isEmpty()) return
        val now = System.currentTimeMillis()
        val existingById = getAllSongsDirect().associateBy { it.songId }
        val toInsert = mutableListOf<SongEntity>()
        val toUpdate = mutableListOf<SongEntity>()
        for (song in songs) {
            val newHash = song.computeMetadataHash()
            val existing = existingById[song.stableId]
            when {
                existing == null -> {
                    toInsert.add(
                        song.toEntity(false,now,now)
                    )
                }
                existing.songHash != newHash -> {
                    toUpdate.add(
                        existing.copy(
                            title        = song.title,
                            duration     = song.duration,
                            artist       = song.artist,
                            album        = song.album,
                            albumArtist  = song.albumArtist,
                            mediaStoreId = song.id.toLong(),
                            songHash = newHash,
                            dateModified = now,
                        )
                    )
                }
            }
        }

        if (toInsert.isNotEmpty()) insertAllBunch(toInsert)
        if (toUpdate.isNotEmpty()) updateBatch(toUpdate)
    }

    @Update
    suspend fun updateBatch(songs: List<SongEntity>)

    @Query("SELECT * FROM songs")
    suspend fun getAllSongsDirect(): List<SongEntity>
}