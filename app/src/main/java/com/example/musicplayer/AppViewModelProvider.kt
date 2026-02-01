package com.example.musicplayer

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.musicplayer.ui.viewModels.MusicViewModel


object AppViewModelProvider{
    val Factory = viewModelFactory {
        initializer {
            MusicViewModel(MusicApplication().container.musicRepository)
        }
    }
}

fun CreationExtras.MusicApplication(): MusicApplication=
    (this[AndroidViewModelFactory.APPLICATION_KEY] as MusicApplication)