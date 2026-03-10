package com.example.musicplayer.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.example.musicplayer.database.table.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SongEntityDao {

    @Query("SELECT * FROM songs ORDER BY dateAdded DESC")
    fun getAllSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE hash = :hash LIMIT 1")
    fun getSongByHash(hash: String): SongEntity?

    @Query("SELECT * FROM songs where mediaStoreId = :id LIMIT 1")
    fun getSongByMediaStore(id: Long): SongEntity?

    @Upsert
    suspend fun  upsertSongs(songs: List<SongEntity>)

    @Upsert
    suspend fun upsertSong(song : SongEntity)

    @Delete
    suspend fun deleteSong(song : SongEntity)
}