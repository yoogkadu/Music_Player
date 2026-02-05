package com.example.musicplayer.ui.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.data.MusicRepository
import com.example.musicplayer.data.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    init {
        Log.d("MusicDebug", "ViewModel Initialized") // Does THIS show up?
        loading()
    }


}