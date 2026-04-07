package com.example.musicplayer.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.musicplayer.database.table.PlaybackEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayBackEventDao {
    @Insert
    suspend fun insertEvent (event : PlaybackEventEntity)
    @Insert
    suspend fun insertEvents (events : List<PlaybackEventEntity>)

    @Query("SELECT * FROM PLAYBACK_EVENTS ORDER BY timestamp DESC LIMIT 50")
    suspend fun getRecentSessionHistorySnapShot() : List<PlaybackEventEntity>

    @Query("SELECT * FROM PLAYBACK_EVENTS ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentSessionHistoryFlow(limit : Int = 50) : Flow<List<PlaybackEventEntity>>

    @Query(
        "SELECT songId, COUNT(*) as skipCount From playback_events "
        + "WHERE eventType = 'SKIP' "
        +"GROUP BY songId ORDER BY skipCount DESC LIMIT 10"

    )
    suspend fun getMostSkippedSongs() : List<SongStats>
    @Query("Delete from playback_events where eventId Not in (Select eventId from playback_events" +
            " order by timestamp desc limit :maxRecords)")
    suspend fun pruneOldEvents(maxRecords: Int = 5000)
}

data class SongStats(val songId: String, val skipCount: Int)