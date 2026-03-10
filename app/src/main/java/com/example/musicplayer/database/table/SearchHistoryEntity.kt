package com.example.musicplayer.database.table

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "search_history",
    foreignKeys = [
        ForeignKey(
            entity = SongEntity::class,
            parentColumns = ["hash"],
            childColumns = ["selectedSongHash"],
            onDelete = ForeignKey.SET_NULL // Keep history even if song is deleted
        )
    ],
    indices = [Index("selectedSongHash")]
)
data class SearchHistoryEntity(
    @PrimaryKey(autoGenerate = true) val searchId: Long = 0,
    val query: String,
    val selectedSongHash: String?,
    val timestamp: Long
)