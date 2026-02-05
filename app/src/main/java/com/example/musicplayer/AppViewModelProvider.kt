package com.example.musicplayer

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.musicplayer.ui.viewModels.BootStrapViewModel
import com.example.musicplayer.ui.viewModels.MusicViewModel


object AppViewModelProvider{
    val Factory = viewModelFactory {
        initializer {
            val application = MusicApplication()
            MusicViewModel(application.container.musicRepository,
                application.container.musicController
            )
        }
        initializer {
            val  application = MusicApplication()
            BootStrapViewModel(
                application.container.mapper,
                application.container.localApplication
                )
        }
    }
}



fun CreationExtras.MusicApplication(): MusicApplication=
    (this[AndroidViewModelFactory.APPLICATION_KEY] as MusicApplication)