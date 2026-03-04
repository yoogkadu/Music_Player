package com.example.musicplayer.data

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.example.musicplayer.api.AndroidMusicFilesApi
import com.example.musicplayer.permission.AndroidPermissionMapper
import com.example.musicplayer.permission.PlatformPermissionMapper

interface AppContainer{
    val musicRepository: MusicRepository
    val mapper : PlatformPermissionMapper
    val localApplication: Application

    val musicController : MusicController

    val dataStore: DataStore<Preferences>
}

class AppDataContainer(private val context: Context ) : AppContainer {

    override val musicRepository: MusicRepository by lazy {
        MainMusicRepository(musicFilesApi = AndroidMusicFilesApi(context.applicationContext))
    }
    override val mapper: PlatformPermissionMapper by lazy {
        AndroidPermissionMapper()
    }
    override val localApplication: Application by lazy {
        context.applicationContext as Application
    }
    override val musicController: MusicController by lazy{
        MusicController(context.applicationContext)
    }
    override val dataStore: DataStore<Preferences> by lazy{
            createDataStoreAndroid(context.applicationContext)
    }


}
