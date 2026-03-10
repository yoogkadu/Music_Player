package com.example.musicplayer.ui.viewModels

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.data.MusicController
import com.example.musicplayer.data.MusicRepository
import com.example.musicplayer.data.Song
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MusicViewModel(
    private val musicRepository: MusicRepository,
    val musicController: MusicController,
    dataStore: DataStore<Preferences>
): ViewModel(){
    val player = musicController.player
    val userPreferencesRepository = UserPreferencesRepository(dataStore)
    private data class LocalMusicState(
        val isLoading: Boolean = false,
        val searchText: String = "",
        val selectedAlbum: String? = null,
        val songs: List<Song> = emptyList(),
        val currentQueue: List<Song> = emptyList(),
        val currentQueueSelection: MusicCurrentQueueSelection = MusicCurrentQueueSelection.SongListSongQueue,
        val albums : Map<String,List<Song>> = emptyMap(),
    )
    private val _localState = MutableStateFlow(LocalMusicState())
    private val _currentPosition = MutableStateFlow(0L)


    val uiState: StateFlow<MusicUiState> = combine(
        _localState,
        musicController.currentMediaId,
        musicController.isPlaying,
        _currentPosition
    ) { local, mediaId, isPlaying, position ->
        val filteredSongs = if (local.searchText.isBlank()) {
            local.songs
        } else {
            local.songs.filter { it.matchSong(local.searchText) }
        }
        val currentSong = if (!mediaId.isNullOrEmpty()) {
            local.songs.find { it.id == mediaId }
        } else null
        MusicUiState(
            songs = local.songs,
            searchedSongs = filteredSongs,
            currentSong = currentSong,
            isPlaying = isPlaying,
            isLoading = local.isLoading,
            searchText = local.searchText,
            currentPosition = position,
            currentQueue = local.currentQueue,
            albums = local.albums,
            selectedAlbum = local.selectedAlbum ?: ""
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MusicUiState()
    )

    fun onSearchTextChange(text: String) {
        _localState.value = _localState.value.copy(searchText = text)
    }


    suspend fun loadDataPreference() {
        val (selection, songId)  = userPreferencesRepository.queueSelectionFlow.first()
        val currentSongs = _localState.value.songs
        if (currentSongs.isNotEmpty()) {
            val song : Song = currentSongs.find { it.id == songId } ?: currentSongs.first()
            loadSong(song, selection)
        }
    }

    private fun loading(){
        viewModelScope.launch {
            try {
                _localState.value=_localState.value.copy(isLoading = true)
                musicRepository.refreshSongs()
                loadDataPreference()


            } catch (e: Exception) {
                // 2. Handle errors so the app doesn't stay stuck loading
                Log.e("MusicVM", "Failed to load music", e)
            } finally {
                // 3. THIS IS CRUCIAL: Always set loading to false
                // regardless of success or failure.
                _localState.value=_localState.value.copy(isLoading = false)
            }
        }
    }
    private fun observeMusicLibrary() {
        viewModelScope.launch {
            musicRepository.observeSongs().collect {
                songs ->
                val albumListMap = songs.filter { it.album.isNotBlank() }.groupBy { it.album }.toSortedMap()
                _localState.value=_localState.value.copy(
                    songs=songs,
                    albums = albumListMap,
                    currentQueue = songs
                )
            }
        }

    }
    fun playSong(song: Song,currentQueueSelection: MusicCurrentQueueSelection) {
        if(_localState.value.currentQueueSelection != currentQueueSelection){
            queueResolver(currentQueueSelection)
        }
        val currentList = uiState.value.currentQueue
        val index = currentList.indexOf(song)
        if (index != -1) {
            musicController.play(currentList, index)
        }
        viewModelScope.launch {
            val currentId = musicController.currentMediaId.value
            userPreferencesRepository.saveQueueSelection(currentQueueSelection, currentId)
        }
    }
    fun loadSong(song: Song,currentQueueSelection: MusicCurrentQueueSelection){
        if(_localState.value.currentQueueSelection != currentQueueSelection){
            queueResolver(currentQueueSelection)
        }
        val currentList = uiState.value.currentQueue
        val index = currentList.indexOf(song)
        if (index != -1) {
            musicController.load(currentList, index)
        }
    }

    fun changeAlbum(albumTitle : String){
        _localState.value=_localState.value.copy(selectedAlbum = albumTitle)
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
    fun queueResolver(currentQueueSelection: MusicCurrentQueueSelection){
        when(currentQueueSelection) {
            is MusicCurrentQueueSelection.PlayListSongQueue -> _localState.value= _localState.value.copy(
                currentQueueSelection = currentQueueSelection,
                currentQueue = _localState.value.albums[currentQueueSelection.album] ?: emptyList()
            )
            is MusicCurrentQueueSelection.SearchedSongQueue -> _localState.value=_localState.value.copy(
                currentQueueSelection = currentQueueSelection,
                currentQueue = uiState.value.searchedSongs
            )
            MusicCurrentQueueSelection.SongListSongQueue -> _localState.value = _localState.value.copy(
                currentQueueSelection=currentQueueSelection,
                currentQueue = _localState.value.songs
            )
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
        observeMusicLibrary()
        viewModelScope.launch {
            while (true) {
                val p = musicController.player.value
                if (p != null && p.isPlaying) {
                    _currentPosition.value = p.currentPosition
                }
                delay(500L)
            }
        }
        viewModelScope.launch {
            // Collect the currentMediaId. Every time it changes (manual click OR auto-skip),
            // this block runs.
            musicController.currentMediaId.collect { mediaId ->
                if (mediaId != null) {
                    // We use a non-blocking launch here
                    userPreferencesRepository.saveQueueSelection(
                        selection = _localState.value.currentQueueSelection,
                        songId = mediaId
                    )
                }
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
    val currentPosition: Long = 0L,
    val currentQueue : List<Song> = emptyList(),
    val albums: Map<String, List<Song>> = emptyMap(),
    val selectedAlbum: String = ""
)

sealed interface MusicCurrentQueueSelection {
    data class SearchedSongQueue(val searchText: String) : MusicCurrentQueueSelection
    data class PlayListSongQueue(val album: String) : MusicCurrentQueueSelection
    object SongListSongQueue : MusicCurrentQueueSelection
}


private object PreferencesKeys {
    val QUEUE_TYPE = stringPreferencesKey("queue_type")
    val QUEUE_ARGUMENT = stringPreferencesKey("queue_argument") // For Album Name or Search Text
    val LAST_SONG_ID = stringPreferencesKey("last_song_id")
}

class UserPreferencesRepository(private val dataStore: DataStore<Preferences>) {
    suspend fun saveQueueSelection(selection: MusicCurrentQueueSelection, songId: String?) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_SONG_ID] = songId ?: ""

            when (selection) {
                is MusicCurrentQueueSelection.PlayListSongQueue -> {
                    preferences[PreferencesKeys.QUEUE_TYPE] = "ALBUM"
                    preferences[PreferencesKeys.QUEUE_ARGUMENT] = selection.album
                }
                is MusicCurrentQueueSelection.SearchedSongQueue -> {
                    preferences[PreferencesKeys.QUEUE_TYPE] = "SEARCH"
                    preferences[PreferencesKeys.QUEUE_ARGUMENT] = selection.searchText
                }
                MusicCurrentQueueSelection.SongListSongQueue -> {
                    preferences[PreferencesKeys.QUEUE_TYPE] = "ALL_SONGS"
                    preferences[PreferencesKeys.QUEUE_ARGUMENT] = ""
                }
            }
        }
    }

    val queueSelectionFlow: Flow<Pair<MusicCurrentQueueSelection, String?>> = dataStore.data
        .map { preferences ->
            val type = preferences[PreferencesKeys.QUEUE_TYPE] ?: "ALL_SONGS"
            val arg = preferences[PreferencesKeys.QUEUE_ARGUMENT] ?: ""
            val songId = preferences[PreferencesKeys.LAST_SONG_ID].let {
                text -> if (text.isNullOrEmpty()) null else text
            }
            val selection = when (type) {
                "ALBUM" -> MusicCurrentQueueSelection.PlayListSongQueue(arg)
                "SEARCH" -> MusicCurrentQueueSelection.SearchedSongQueue(arg)
                else -> MusicCurrentQueueSelection.SongListSongQueue
            }
            Pair(selection, songId)
        }
}
