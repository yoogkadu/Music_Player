package com.example.musicplayer.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.data.MusicRepository
import com.example.musicplayer.data.Song
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MusicViewModel(
    private val musicRepository: MusicRepository
): ViewModel(){
    val songs: StateFlow<List<Song>> = musicRepository.observeSongs().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )
    init {
        refreshMusic()
    }
    fun refreshMusic(){
        viewModelScope.launch {
            musicRepository.refreshSongs()
        }

    }


}