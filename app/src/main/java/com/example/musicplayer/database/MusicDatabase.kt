package com.example.musicplayer.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.musicplayer.database.dao.AudioAnalysisDao
import com.example.musicplayer.database.dao.LyricsDao
import com.example.musicplayer.database.dao.PlayBackEventDao
import com.example.musicplayer.database.dao.PlaylistDao
import com.example.musicplayer.database.dao.SongEntityDao
import com.example.musicplayer.database.table.AudioAnalysisEntity
import com.example.musicplayer.database.table.LyricsEntity
import com.example.musicplayer.database.table.PlaybackEventEntity
import com.example.musicplayer.database.table.PlaylistEntity
import com.example.musicplayer.database.table.PlaylistSongCrossRef
import com.example.musicplayer.database.table.SongEntity

@Database(
    version = 1,
    exportSchema = true,
    entities = [
        SongEntity::class,
        PlaylistEntity::class,
        PlaybackEventEntity::class,
        PlaylistSongCrossRef::class,
        AudioAnalysisEntity::class,
        LyricsEntity::class
    ]
)
abstract class MusicDatabase: RoomDatabase() {
    abstract fun songEntityDao() : SongEntityDao
    abstract fun playlistDao() : PlaylistDao
    abstract fun playbackEventDao() : PlayBackEventDao
    abstract fun audioAnalysisDao() : AudioAnalysisDao
    abstract fun lyricsDao() : LyricsDao
}