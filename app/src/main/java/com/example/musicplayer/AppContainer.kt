package com.example.musicplayer

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.example.musicplayer.api.AndroidMusicFilesApi
import com.example.musicplayer.data.MainMusicRepository
import com.example.musicplayer.data.MusicController
import com.example.musicplayer.data.MusicRepository
import com.example.musicplayer.data.createDataStoreAndroid
import com.example.musicplayer.database.MusicDatabase
import com.example.musicplayer.database.getAndroidDatabase
import com.example.musicplayer.permission.AndroidPermissionMapper
import com.example.musicplayer.permission.PlatformPermissionMapper

interface AppContainer{
    val musicRepository: MusicRepository
    val mapper : PlatformPermissionMapper
    val localApplication: Application

    val musicController : MusicController

    val dataStore: DataStore<Preferences>
    val database : MusicDatabase
}

class AppDataContainer(private val context: Context ) : AppContainer {
    val applicationContext: Context = context.applicationContext

    override val musicRepository: MusicRepository by lazy {
        MainMusicRepository(musicFilesApi = AndroidMusicFilesApi(applicationContext))
    }
    override val mapper: PlatformPermissionMapper by lazy {
        AndroidPermissionMapper()
    }
    override val localApplication: Application by lazy {
        applicationContext as Application
    }
    override val musicController: MusicController by lazy{
        MusicController(applicationContext)
    }
    override val dataStore: DataStore<Preferences> by lazy{
        createDataStoreAndroid(applicationContext)
    }
    override val database: MusicDatabase by lazy {
        getAndroidDatabase(applicationContext)
    }

}
