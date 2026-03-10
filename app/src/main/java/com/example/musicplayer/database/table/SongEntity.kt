package com.example.musicplayer.database.table

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey val hash: String,
    val mediaStoreId : Long,
    val title: String,
    val duration: Long,
    val uri: String,
    val artist: String,
    val album: String,
    val albumArtist: String,
    val isFavorite : Boolean,
    val dateAdded : Long,
    val dateModified : Long,
)
