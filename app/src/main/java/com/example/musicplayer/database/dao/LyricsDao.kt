package com.example.musicplayer.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.musicplayer.database.table.LyricsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LyricsDao {
    @Upsert
    suspend fun upsertLyrics(lyrics: LyricsEntity)

    @Query("SELECT * FROM lyrics WHERE songHash = :hash LIMIT 1")
    fun getLyricsFlow(hash: String): Flow<LyricsEntity?>

    @Query("DELETE FROM lyrics WHERE songHash = :hash")
    suspend fun deleteLyrics(hash: String)
}