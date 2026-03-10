package com.example.musicplayer.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.example.musicplayer.database.table.SearchHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchHistoryDao {
    @Insert
    suspend fun insertSearch(history: SearchHistoryEntity)

    // Gets the most recent searches for the search bar dropdown
    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentSearchesFlow(limit : Int = 10): Flow<List<SearchHistoryEntity>>

    @Query("DELETE FROM search_history")
    suspend fun clearHistory()
    @RawQuery
    suspend fun vacuumDatabase(query: SupportSQLiteQuery): Int
}