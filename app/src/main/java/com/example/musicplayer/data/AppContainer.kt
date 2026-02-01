package com.example.musicplayer.data

import android.content.Context
import com.example.musicplayer.api.FakeMusicFilesApi

interface AppContainer{
    val musicRepository: MusicRepository
}

class AppDataContainer(private val context: Context) : AppContainer {

    override val musicRepository: MusicRepository by lazy {
        MainMusicRepository(musicFilesApi = FakeMusicFilesApi())
    }

}
