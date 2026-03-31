package com.example.musicplayer.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.musicplayer.database.table.AudioAnalysisEntity
import com.example.musicplayer.database.table.SongEntity

@Dao
interface AudioAnalysisDao {
    @Upsert
    suspend fun upsertAudioAnalysis(audioAnalysis: AudioAnalysisEntity)

    @Query("SELECT * FROM AUDIO_ANALYSIS WHERE songHash = :hash LIMIT 1")
    suspend fun getAnalysis(hash : String) : AudioAnalysisEntity?

    @Query("SELECT * FROM songs where hash not in " +
            "(select songhash from audio_analysis where isEssentialAnalyzed = 1)")
    suspend fun getSongNeedingEssentiaScan(): List<SongEntity>

    @Query("SELECT * FROM audio_analysis WHERE isVggAnalyzed = 0")
    suspend fun getAnalysesNeedingVggish() : List<AudioAnalysisEntity>
}