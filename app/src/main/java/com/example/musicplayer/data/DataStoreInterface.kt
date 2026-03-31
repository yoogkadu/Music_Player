package com.example.musicplayer.data

import com.example.musicplayer.ui.viewModels.MusicCurrentQueueSelection
import kotlinx.coroutines.flow.Flow

interface DataStoreInterface {
    suspend fun saveQueueSelection(selection: MusicCurrentQueueSelection, songId: String?)
    val queueSelectionFlow: Flow<Pair<MusicCurrentQueueSelection, String?>>
}