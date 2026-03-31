package com.example.musicplayer.ui.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.data.DataStoreInterface
import com.example.musicplayer.data.MusicController
import com.example.musicplayer.data.MusicRepository
import com.example.musicplayer.data.Song
import com.example.musicplayer.database.DataBaseInterface
import com.example.musicplayer.database.table.PlaylistEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MusicViewModel(
    private val musicRepository: MusicRepository,
    val musicController: MusicController,
    private val dataStoreInterface: DataStoreInterface,
    private val dataBaseInterface: DataBaseInterface
): ViewModel(){
    val player = musicController.player
    private data class LocalMusicState(
        val isLoading: Boolean = false,
        val searchText: String = "",
        val selectedAlbum: String? = null,
        val songs: List<Song> = emptyList(),
        val currentQueue: List<Song> = emptyList(),
        val currentQueueSelection: MusicCurrentQueueSelection = MusicCurrentQueueSelection.SongListSongQueue,
        val albums : Map<String,List<Song>> = emptyMap(),
        val playlists : Map<String,List<Song>> = emptyMap()
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
            selectedAlbum = local.selectedAlbum ?: "",
            albums = local.albums,
            playlists = local.playlists
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
        val (selection, songId)  = dataStoreInterface.queueSelectionFlow.first()
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
            dataStoreInterface.saveQueueSelection(currentQueueSelection, currentId)
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
            is MusicCurrentQueueSelection.AlbumSongQueue -> _localState.value= _localState.value.copy(
                currentQueueSelection = currentQueueSelection,
                currentQueue = _localState.value.albums[currentQueueSelection.album] ?: emptyList()
            )
            is MusicCurrentQueueSelection.SearchedSongQueue -> _localState.value=_localState.value.copy(
                currentQueueSelection = currentQueueSelection,
                currentQueue = uiState.value.searchedSongs
            )
            is MusicCurrentQueueSelection.SongListSongQueue -> _localState.value = _localState.value.copy(
                currentQueueSelection=currentQueueSelection,
                currentQueue = _localState.value.songs
            )
            is MusicCurrentQueueSelection.PlaylistSongQueue -> _localState.value = _localState.value.copy(
                currentQueueSelection=currentQueueSelection,

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


    @OptIn(ExperimentalCoroutinesApi::class)
    fun loadAllPlaylists() {
        viewModelScope.launch {
            // We combine the playlist stream with the master song list
            combine(
                dataBaseInterface.getPlaylist(),
                _localState.map { it.songs }.distinctUntilChanged()
            ) { playlistEntities, masterSongList ->
                playlistEntities to masterSongList
            }.flatMapLatest { (entities, masterSongs) ->
                if (entities.isEmpty()) return@flatMapLatest flowOf(emptyMap<String, List<Song>>())

                val playlistFlows = entities.map { entity ->
                    dataBaseInterface.getSongsFromPlaylistId(entity.playlistId).map { songEntities ->
                        val songsInPlaylist = songEntities.mapNotNull { songEntity ->
                            masterSongs.find { it.id == songEntity.hash }
                        }
                        entity.name to songsInPlaylist
                    }
                }
                combine(playlistFlows) { it.toMap() }
            }.collect { playlistMap ->
                _localState.value = _localState.value.copy(playlists = playlistMap)
            }
        }
    }
    init {
        loading()
        observeMusicLibrary()
        loadAllPlaylists()
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
            musicController.currentMediaId.collect { mediaId ->
                if (mediaId != null) {
                    dataStoreInterface.saveQueueSelection(
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
    val playlists: Map<String, List<Song>> = emptyMap(),
    val selectedAlbum: String = ""

)

sealed interface MusicCurrentQueueSelection {
    data class SearchedSongQueue(val searchText: String) : MusicCurrentQueueSelection
    data class AlbumSongQueue(val album: String) : MusicCurrentQueueSelection
    object SongListSongQueue : MusicCurrentQueueSelection

    data class PlaylistSongQueue(val playlistId : Int) : MusicCurrentQueueSelection
}


