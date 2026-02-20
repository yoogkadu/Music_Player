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
{
    fun matchSong(query: String): Boolean {
        val matchingCombination  = listOf(
            "$artist $album",
            "$title $artist",
            "$title $album",
            "$title $albumArtist",
            "$artist $album",
            artist,
            title,
            album,
            albumArtist,
        )
        return matchingCombination.any {
            it.contains(query, ignoreCase = true)
        }

    }
}