package com.example.musicplayer.database.table

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "lyrics",
    foreignKeys = [
        ForeignKey(
            entity = SongEntity::class,
            parentColumns = ["hash"],
            childColumns = ["songHash"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("songHash")]
)
data class LyricsEntity(
    @PrimaryKey val songHash: String,
    val rawContent: String,
    val source: String, // EMBEDDED, LOCAL_FILE
    val isSynced: Boolean
)