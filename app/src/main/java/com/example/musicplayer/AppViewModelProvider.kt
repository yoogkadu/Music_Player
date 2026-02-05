package com.example.musicplayer

import android.app.Application
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.musicplayer.ui.viewModels.BootStrapViewModel
import com.example.musicplayer.ui.viewModels.MusicViewModel


object AppViewModelProvider{
    val Factory = viewModelFactory {
        initializer {
            MusicViewModel(MusicApplication().container.musicRepository)
        }
        initializer {
            val application = MusicApplication()
            BootStrapViewModel(
                application.container.mapper,
                application.container.LocalApplication
                )
        }
    }
}



fun CreationExtras.MusicApplication(): MusicApplication=
    (this[AndroidViewModelFactory.APPLICATION_KEY] as MusicApplication)