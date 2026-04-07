package com.example.musicplayer.ui.viewModels

import android.util.Log
import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.data.DataStoreInterface
import com.example.musicplayer.data.MusicController
import com.example.musicplayer.data.MusicRepository
import com.example.musicplayer.data.Song
import com.example.musicplayer.database.DataBaseInterface
import com.example.musicplayer.database.table.PlaylistEntity
import com.example.musicplayer.database.table.PlaylistSongCrossRef
import com.example.musicplayer.database.table.SongEntity
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
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock

class MusicViewModel(
    private val musicRepository: MusicRepository,
    val musicController: MusicController,
    private val dataStoreInterface: DataStoreInterface,
    private val dataBaseInterface: DataBaseInterface
): ViewModel(){
    val player = musicController.player
    private data class LocalMusicState(
        val searchText: String = "",
        val selectedAlbum: String? = null,
        val currentQueue: List<Song> = emptyList(),
        val currentQueueSelection: MusicCurrentQueueSelection = MusicCurrentQueueSelection.SongListSongQueue,
        val currentMediaId : String? = null,
        val isPlaying: Boolean = false,
        val playListName : String? = null
    )
    private data class SlowUpdateMusicState(
        val isLoading: Boolean = false,
        val songs: List<Song> = emptyList(),
        val albums : Map<String,List<Song>> = emptyMap(),
        val playlists : Map<PlaylistEntity,List<Song>> = emptyMap()
    )
    private data class FastUpdateMusicState(
        val currentPosition: Long = 0L
    )
    private val _localState = MutableStateFlow(LocalMusicState())
    private val _slowUpdateLocalState = MutableStateFlow(SlowUpdateMusicState())
    private val _fastUpdateLocalState = MutableStateFlow(FastUpdateMusicState())

    val uiState: StateFlow<MusicUiState> = combine(
        _localState,
        _slowUpdateLocalState,
        _fastUpdateLocalState
    ) { local, slow, fast ->
        val filteredSongs = if (local.searchText.isBlank()) {
            slow.songs
        } else {
            slow.songs.filter { it.matchSong(local.searchText) }
        }
        val currentSong = slow.songs.find { it.id == local.currentMediaId }
        MusicUiState(
            songs = slow.songs,
            searchedSongs = filteredSongs,
            currentSong = currentSong,
            isPlaying = local.isPlaying,
            isLoading = slow.isLoading,
            searchText = local.searchText,
            currentPosition = fast.currentPosition,
            currentQueue = local.currentQueue,
            selectedAlbum = local.selectedAlbum ?: "",
            albums = slow.albums,
            playlists = slow.playlists
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
        val currentSongs = _slowUpdateLocalState.value.songs
        if (currentSongs.isNotEmpty()) {
            val song : Song = currentSongs.find { it.id == songId } ?: currentSongs.first()
            loadSong(song, selection)
        }
    }
    private fun loading(){
        viewModelScope.launch {
            try {
                _slowUpdateLocalState.update { it.copy(isLoading = true) }
                musicRepository.refreshSongs()
                loadDataPreference()
            } catch (e: Exception) {
                // 2. Handle errors so the app doesn't stay stuck loading
                Log.e("MusicVM", "Failed to load music", e)
            } finally {
                // 3. THIS IS CRUCIAL: Always set loading to false
                // regardless of success or failure.
                _slowUpdateLocalState.update { it.copy(isLoading = false) }
            }
        }
    }
    private fun observeMusicLibrary() {
        viewModelScope.launch {
            musicRepository.observeSongs().collect {
                songs ->
                val albumListMap = songs.filter { it.album.isNotBlank() }.groupBy { it.album }.toSortedMap()
                _slowUpdateLocalState.update {
                    it.copy(songs=songs, albums = albumListMap)
                }
                _localState.update { it.copy(currentQueue = songs) }
                if (songs.isNotEmpty()) {
                    addOrUpdateSongsInDb(songs)
                }
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
                currentQueue = _slowUpdateLocalState.value.albums[currentQueueSelection.album] ?: emptyList()
            )
            is MusicCurrentQueueSelection.SearchedSongQueue -> _localState.value=_localState.value.copy(
                currentQueueSelection = currentQueueSelection,
                currentQueue = uiState.value.searchedSongs
            )
            is MusicCurrentQueueSelection.SongListSongQueue -> _localState.value = _localState.value.copy(
                currentQueueSelection=currentQueueSelection,
                currentQueue = _slowUpdateLocalState.value.songs
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
        _fastUpdateLocalState.update {
            it.copy(currentPosition = 0)
        }
       player.value?.seekToNext()
    }

    fun skipToPrevious() {
        _fastUpdateLocalState.update {
            it.copy(currentPosition = 0)
        }
        player.value?.seekToPrevious()
    }
    fun addOrUpdateSongsInDb(songs : List<Song>){
        viewModelScope.launch {
            dataBaseInterface.addOrUpdateSongs(songs)
        }
    }

    fun createPlaylistAndAddSongs(playlistName : String, songList : List<Song>){
        viewModelScope.launch {
            dataBaseInterface.createPlaylistAndAddSongs(playlistName = playlistName,songList)
        }
    }
    fun loadAllPlaylists() {
        viewModelScope.launch {
            combine(
                dataBaseInterface.getAllPlaylistSongs(),
                _slowUpdateLocalState.map { it.songs }.distinctUntilChanged()
            ) { playlistSongs, masterSongs ->
                playlistSongs.groupBy(
                    keySelector = { PlaylistEntity(playlistId = it.playlistId, name = it.playlistName, createdAt = it.createdAt) },
                    valueTransform = { masterSongs.find { song -> song.matchSongWithEntity(it.songId) } }
                ).mapValues { entry -> entry.value.filterNotNull() }
            }.collect { map ->
                _slowUpdateLocalState.update {
                    it.copy(playlists = map)
                }
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
                    _fastUpdateLocalState.update { it.copy(currentPosition = p.currentPosition) }
                }
                delay(500L)
            }
        }
        viewModelScope.launch {
            musicController.currentMediaId.collect { mediaId ->
                _localState.update {
                    it.copy(currentMediaId = mediaId) }
                if(mediaId!=null){
                    dataStoreInterface.saveQueueSelection(
                        selection = _localState.value.currentQueueSelection,
                        songId = mediaId
                    )
                }
            }
        }
        viewModelScope.launch {
            musicController.isPlaying.collect {
                playing ->
                _localState.update { it.copy(isPlaying = playing) }
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
    val playlists: Map<PlaylistEntity, List<Song>> = emptyMap(),
    val selectedAlbum: String = ""
)

sealed interface MusicCurrentQueueSelection {
    data class SearchedSongQueue(val searchText: String) : MusicCurrentQueueSelection
    data class AlbumSongQueue(val album: String) : MusicCurrentQueueSelection
    object SongListSongQueue : MusicCurrentQueueSelection
    data class PlaylistSongQueue(val playlistName : String) : MusicCurrentQueueSelection
}


