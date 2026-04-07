package com.example.musicplayer.database

import android.content.Context
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

fun getAndroidDatabase(context: Context) : MusicDatabase {
    val dbFile = context.getDatabasePath("music.db")
    return Room.databaseBuilder<MusicDatabase>(
        context.applicationContext,
        name = dbFile.absolutePath,
    ).setDriver(BundledSQLiteDriver())
        .fallbackToDestructiveMigration(true)
        .build()
}