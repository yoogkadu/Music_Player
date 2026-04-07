package com.example.musicplayer.database.table

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "songs", indices = [
    Index("mediaStoreId", unique = true),
    Index("duration")
])
data class SongEntity(
    @PrimaryKey val songId: String,
    val mediaStoreId : Long,
    val title: String,
    val duration: Long,
    val artist: String,
    val album: String,
    val albumArtist: String,
    val isFavorite : Boolean,
    val dateAdded : Long,
    val dateModified : Long,
)
