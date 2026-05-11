package com.iptvplayer.data.local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.iptvplayer.data.local.entities.ChannelEntity
import com.iptvplayer.data.local.entities.EpgProgrammeEntity
import com.iptvplayer.data.local.entities.PlaylistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY addedAt DESC")
    fun getAll(): Flow<List<PlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(playlist: PlaylistEntity)

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun delete(id: String)

    @Query("SELECT * FROM playlists WHERE id = :id")
    suspend fun getById(id: String): PlaylistEntity?
}

@Dao
interface ChannelDao {
    @Query("SELECT * FROM channels WHERE playlistId = :playlistId ORDER BY number ASC")
    fun getChannelsByPlaylist(playlistId: String): Flow<List<ChannelEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(channels: List<ChannelEntity>)

    @Query("DELETE FROM channels WHERE playlistId = :playlistId")
    suspend fun deleteByPlaylist(playlistId: String)
}

@Dao
interface EpgProgrammeDao {
    @Query(
        """
        SELECT * FROM epg_programmes
        WHERE channelId = :channelId
          AND startAt >= :fromEpoch
          AND endAt <= :toEpoch
        ORDER BY startAt ASC
        """
    )
    fun getByChannelAndTimeRange(channelId: String, fromEpoch: Long, toEpoch: Long): Flow<List<EpgProgrammeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(programmes: List<EpgProgrammeEntity>)

    @Query("DELETE FROM epg_programmes WHERE startAt < :beforeEpoch")
    suspend fun deleteStale(beforeEpoch: Long)
}
