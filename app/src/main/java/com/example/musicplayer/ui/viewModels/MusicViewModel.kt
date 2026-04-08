package com.example.musicplayer.ui.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.example.musicplayer.data.DataStoreInterface
import com.example.musicplayer.data.MusicController
import com.example.musicplayer.data.MusicRepository
import com.example.musicplayer.data.Song
import com.example.musicplayer.database.DataBaseInterface
import com.example.musicplayer.database.table.PlaylistEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
        val playListName : String? = null,
        val unshuffledQueueIndex : List<String> = emptyList(),
        val isShuffled : Boolean = false
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
            playlists = slow.playlists,
            isShuffled = local.isShuffled
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
            is MusicCurrentQueueSelection.PlaylistSongQueue -> {
                val targetPlaylist = _slowUpdateLocalState.value.playlists.keys.find {
                    it.playlistId==currentQueueSelection.playlistId.toLong()
                }
                _localState.update {
                    it.copy(
                        currentQueueSelection = currentQueueSelection,
                        currentQueue = _slowUpdateLocalState.value.playlists[targetPlaylist] ?: emptyList()
                    )
                }
            }


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
    fun toggleShuffle(){
        val currentState = _localState.value
        if(!currentState.isShuffled){
            val unShuffledQueue = currentState.currentQueue.map { it.id }
            val currentSong = currentState.currentQueue.find { it.id ==currentState.currentMediaId }
            val otherSongs = currentState.currentQueue.filter { it.id != currentState.currentMediaId }.shuffled()
            val shuffledQueue = listOfNotNull(currentSong) + otherSongs
            _localState.update {
                it.copy(
                    isShuffled = true,
                    unshuffledQueueIndex = unShuffledQueue,
                    currentQueue = shuffledQueue
                )
            }
            updateQueueSeamlessly(shuffledQueue, 0,_fastUpdateLocalState.value.currentPosition)
        }
        else{
            val originalIds = currentState.unshuffledQueueIndex
            val masterSongs = _slowUpdateLocalState.value.songs
            val originalQueue = originalIds.mapNotNull { id ->
                masterSongs.find { it.id == id }
            }
            val newIndex = originalQueue.indexOfFirst { it.id == currentState.currentMediaId }
            _localState.update {
                it.copy(
                    isShuffled = false,
                    currentQueue = originalQueue,
                    unshuffledQueueIndex = emptyList()
                )
            }
            musicController.load(originalQueue, if (newIndex != -1) newIndex else 0)
        }
    }
    fun updateQueueSeamlessly(songs: List<Song>, startIndex: Int, startPositionMs: Long) {
        val mediaItems = songs.map { it.toMediaItem() }

        player.value?.setMediaItems(mediaItems, startIndex, startPositionMs)
    }
    fun Song.toMediaItem(): MediaItem {
        return MediaItem.Builder()
            .setMediaId(this.id) // Use your stable ID string here
            .setUri(this.uri)    // The content URI from MediaStore
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(this.title)
                    .setArtist(this.artist)
                    .setAlbumTitle(this.album)
                    .build()
            )
            .build()
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
                val songLookup = masterSongs.associateBy { it.stableId }
                playlistSongs.groupBy(
                    keySelector = {
                        playlistObj ->
                        PlaylistEntity(playlistObj.playlistId,playlistObj.playlistName,playlistObj.createdAt)
                    },
                    valueTransform = {songLookup[it.songId]}
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
    val selectedAlbum: String = "",
    val isShuffled: Boolean = false
)

sealed interface MusicCurrentQueueSelection {
    data class SearchedSongQueue(val searchText: String) : MusicCurrentQueueSelection
    data class AlbumSongQueue(val album: String) : MusicCurrentQueueSelection
    object SongListSongQueue : MusicCurrentQueueSelection
    data class PlaylistSongQueue(val playlistId : String) : MusicCurrentQueueSelection
}


