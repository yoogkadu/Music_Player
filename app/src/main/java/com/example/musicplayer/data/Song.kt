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
    val albumArtist: String,
)
{
    val stableId : String get() = this.id
    fun matchSong(query: String): Boolean {
        val matchingCombination  = listOf(
            "$artist $album",
            "$title $artist",
            "$title $album", 
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
    fun matchSongWithEntity(songEntityId: String): Boolean {
        return songEntityId==this.id
    }

}