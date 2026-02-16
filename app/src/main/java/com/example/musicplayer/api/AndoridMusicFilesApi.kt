package com.example.musicplayer.api

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import androidx.core.net.toUri
import com.example.musicplayer.data.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class AndroidMusicFilesApi (private val context : Context) : MusicFilesApi {
    private val _songFlow = MutableStateFlow<List<Song>>(emptyList())
    override fun getSongs(): Flow<List<Song>> = _songFlow.asStateFlow()
    override suspend fun refreshSongs() {
        withContext(Dispatchers.IO){
            val songList = mutableListOf<Song>()

            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ARTIST,
            )
            val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
            val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"

            val uri= MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

            val query = context.contentResolver.query(
                uri,
                projection,
                selection,
                null,
                sortOrder
            )
            query?.use {
                try {
                    val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                    val titleColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                    val durationColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                    val artistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                    val albumArtColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                    val albumColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                    val albumArtistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ARTIST)

                    while(it.moveToNext()){
                        val id = it.getLong(idColumn)
                        val title = it.getString(titleColumn) ?: "Unknown Title"
                        val duration = it.getLong(durationColumn)
                        val artist = it.getString(artistColumn) ?: "Unknown Artist"
                        val album = it.getString(albumColumn) ?: "Unknown Album"
                        val albumId = it.getLong(albumArtColumn)
                        val albumArtist = it.getString(albumArtistColumn) ?: "Unknown Album Artist"

                        val contentUri = ContentUris.withAppendedId(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            id
                        )
                        val albumArtUri = ContentUris.withAppendedId(
                            "content://media/external/audio/albumart".toUri(),
                             albumId
                        )

                        songList.add(
                            Song(
                                id = id.toString(),
                                title = title,
                                artist = artist,
                                album = album,
                                duration = duration,
                                uri = contentUri,
                                albumArtUri = albumArtUri,
                                albumArtist = albumArtist
                            )
                        )

                    }


                }
                catch (e: Exception){
                    e.printStackTrace()
                    return@use
                }
            }

            _songFlow.value= songList
        }

    }

}