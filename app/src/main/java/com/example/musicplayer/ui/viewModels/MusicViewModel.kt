package com.example.musicplayer.ui.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.data.MusicController
import com.example.musicplayer.data.MusicRepository
import com.example.musicplayer.data.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MusicViewModel(
    private val musicRepository: MusicRepository,
    val musicController: MusicController
): ViewModel(){

    val songs: StateFlow<List<Song>> = musicRepository.observeSongs().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )
    val player = musicController.player

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong = _currentSong.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

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
        musicController.play(song)
        _currentSong.value = song
    }

    fun stopSong() {
        musicController.release()
        _currentSong.value = null
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

    fun skipToNext() {
        // Logic to find the next song in the list
        val currentList = songs.value
        val currentIndex = currentList.indexOf(_currentSong.value)
        if (currentIndex != -1 && currentIndex < currentList.size - 1) {
            playSong(currentList[currentIndex + 1])
        }
    }

    fun skipToPrevious() {
        val currentList = songs.value
        val currentIndex = currentList.indexOf(_currentSong.value)
        if (currentIndex > 0) {
            playSong(currentList[currentIndex - 1])
        }
    }
    init {

        loading()
    }


}