package com.example.musicplayer.ui.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.data.MusicController
import com.example.musicplayer.data.MusicRepository
import com.example.musicplayer.data.Song
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MusicViewModel(
    private val musicRepository: MusicRepository,
    val musicController: MusicController
): ViewModel(){

    val songs: StateFlow<List<Song>> = musicRepository
        .observeSongs()
        .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    val player = musicController.player

    val currentSong: StateFlow<Song?> = musicController.currentMediaId
        .combine(songs) { id, songList ->
            if (id != null && id.isNotEmpty()) {
                songList.find { it.id == id }
            } else {
                null
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )
    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()
    val isPlaying: StateFlow<Boolean> = musicController.isPlaying

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition = _currentPosition.asStateFlow()


    private fun loading(){
        viewModelScope.launch {
            try {
                _isLoading.value = true
                // 1. Perform the actual scan
                musicRepository.refreshSongs()
            } catch (e: Exception) {
                // 2. Handle errors so the app doesn't stay stuck loading
                Log.e("MusicVM", "Failed to load music", e)
            } finally {
                // 3. THIS IS CRUCIAL: Always set loading to false
                // regardless of success or failure.
                _isLoading.value = false
            }
        }
    }
    fun playSong(song: Song) {
        val currentList = songs.value
        val index = currentList.indexOf(song)
        if (index != -1) {
            musicController.play(currentList, index)
        }
    }


    fun togglePlayPause(){
        val p = player.value ?: return
        if(p.isPlaying){
            p.pause()
        }
        else{
            p.play()
        }
    }

    fun stopSong(){
        player.value?.stop()
    }
    fun skipToNext() {
        _currentPosition.value=0
       player.value?.seekToNext()
    }

    fun skipToPrevious() {
        _currentPosition.value=0
        player.value?.seekToPrevious()
    }
    init {
        loading()
        viewModelScope.launch {
            while (true) {
                // Check if the player is ready and currently playing
                val p = musicController.player.value
                if (p != null && p.isPlaying) {
                    _currentPosition.value = p.currentPosition
                }
                // 500ms is a good balance for M14 performance vs smoothness
                delay(500L)
            }
        }
    }
}
