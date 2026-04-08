package com.example.musicplayer.data

import android.net.Uri
import com.example.musicplayer.database.table.SongEntity

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
    fun computeMetadataHash(): String {
        val raw = "$title|$artist|$album|$albumArtist|$duration"
        return raw.hashCode().toString()
    }
    fun toEntity(isFavorite : Boolean = false,dateAdded: Long, dateModified : Long) : SongEntity{
        return SongEntity(
            songId = this.stableId,
            mediaStoreId = this.id.toLong(),
            title = this.title,
            duration = this.duration,
            artist = this.artist,
            album = this.album,
            albumArtist = this.albumArtist,
            isFavorite = isFavorite,
            dateAdded = dateAdded,
            dateModified = dateModified,
            songHash = this.computeMetadataHash(),
        )
    }

}