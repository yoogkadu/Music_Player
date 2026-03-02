package com.example.musicplayer.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

fun createDataStoreAndroid(context: Context) : DataStore<Preferences> {
    return createDataStore { context.filesDir.resolve(DATA_STORE_FILE_NAME).absolutePath }
}