package com.example.musicplayer.data

import android.net.Uri

data class Song(
    val id: String,
    val title: String,
    val duration: Long,
    val uri: Uri?,
    val artist: String,
    val albumArtUri: Uri?,
    val album: String,
    val albumArtist: String
)
