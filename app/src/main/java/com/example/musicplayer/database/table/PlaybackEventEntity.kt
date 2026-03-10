package com.example.musicplayer.database.table

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "playback_events",
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
data class PlaybackEventEntity(
    @PrimaryKey(autoGenerate = true) val eventId: Long = 0,
    val songHash: String,
    val sessionId: String, // UUID to group events
    val eventType: String, // PLAY, SKIP, REPEAT
    val listenDuration: Long,
    val timestamp: Long,
    val isShuffled: Boolean
)