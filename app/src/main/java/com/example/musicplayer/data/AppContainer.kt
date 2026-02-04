package com.example.musicplayer.data

import android.app.Application
import android.content.Context
import com.example.musicplayer.api.FakeMusicFilesApi
import com.example.musicplayer.permission.AndroidPermissionMapper
import com.example.musicplayer.permission.PlatformPermissionMapper

interface AppContainer{
    val musicRepository: MusicRepository
    val mapper : PlatformPermissionMapper
    val LocalApplication: Application
}

class AppDataContainer(private val context: Context) : AppContainer {

    override val musicRepository: MusicRepository by lazy {
        MainMusicRepository(musicFilesApi = FakeMusicFilesApi())
    }
    override val mapper: PlatformPermissionMapper by lazy {
        AndroidPermissionMapper()
    }
    override val LocalApplication: Application by lazy {
        context.applicationContext as Application
    }
}
