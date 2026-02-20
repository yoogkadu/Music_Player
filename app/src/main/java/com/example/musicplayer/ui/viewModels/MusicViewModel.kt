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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MusicViewModel(
    private val musicRepository: MusicRepository,
    val musicController: MusicController
): ViewModel(){
    private val _songs = musicRepository.observeSongs()
    val player = musicController.player

    private val _isLoading = MutableStateFlow(true)

    private val _currentPosition = MutableStateFlow(0L)


    private val _searchText = MutableStateFlow("")

    private val playerState = combine(
        musicController.currentMediaId,
        musicController.isPlaying,
        _currentPosition
    ) { id, playing, pos ->
        Triple(id, playing, pos)
    }

    val uiState: StateFlow<MusicUiState> = combine(
        _songs,
        _searchText,
        _isLoading,
        playerState
    ) { songs, text, loading, player ->
        val (mediaId, isPlaying, position) = player

        val current = if (!mediaId.isNullOrEmpty()) songs.find { it.id == mediaId } else null
        val filtered = if (text.isBlank()) songs else songs.filter { it.matchSong(text) }

        MusicUiState(
            songs = songs,
            searchedSongs = filtered,
            currentSong = current,
            isPlaying = isPlaying,
            isLoading = loading,
            searchText = text,
            currentPosition = position
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MusicUiState())

    fun onSearchTextChange(text: String) {
        Log.d("MusicVM",text)
        _searchText.value = text
    }


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
        val currentList = uiState.value.songs
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

data class MusicUiState(
    val songs: List<Song> = emptyList(),
    val searchedSongs: List<Song> = emptyList(),
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val isLoading: Boolean = true,
    val searchText: String = "",
    val currentPosition: Long = 0L
)
