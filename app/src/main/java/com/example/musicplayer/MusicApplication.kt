package com.example.musicplayer

import android.app.Application
import com.example.musicplayer.data.AppContainer
import com.example.musicplayer.data.AppDataContainer

class MusicApplication : Application() {

    /**
     * AppContainer instance used by the rest of classes to obtain dependencies
     */
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}
