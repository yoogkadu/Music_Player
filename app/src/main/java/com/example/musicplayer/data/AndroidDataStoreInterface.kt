package com.example.musicplayer.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.musicplayer.ui.viewModels.MusicCurrentQueueSelection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AndroidDataStoreInterface(private val dataStore: DataStore<Preferences>) : DataStoreInterface {

    private object PreferencesKeys {
        val QUEUE_TYPE = stringPreferencesKey("queue_type")
        val QUEUE_ARGUMENT = stringPreferencesKey("queue_argument") // For Album Name or Search Text
        val LAST_SONG_ID = stringPreferencesKey("last_song_id")
    }

    override suspend fun saveQueueSelection(selection: MusicCurrentQueueSelection, songId: String?) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_SONG_ID] = songId ?: ""

            when (selection) {
                is MusicCurrentQueueSelection.AlbumSongQueue -> {
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

    override val queueSelectionFlow: Flow<Pair<MusicCurrentQueueSelection, String?>> = dataStore.data
        .map { preferences ->
            val type = preferences[PreferencesKeys.QUEUE_TYPE] ?: "ALL_SONGS"
            val arg = preferences[PreferencesKeys.QUEUE_ARGUMENT] ?: ""
            val songId = preferences[PreferencesKeys.LAST_SONG_ID].let { text ->
                if (text.isNullOrEmpty()) null else text
            }
            val selection = when (type) {
                "ALBUM" -> MusicCurrentQueueSelection.AlbumSongQueue(arg)
                "SEARCH" -> MusicCurrentQueueSelection.SearchedSongQueue(arg)
                else -> MusicCurrentQueueSelection.SongListSongQueue
            }
            Pair(selection, songId)
        }
}