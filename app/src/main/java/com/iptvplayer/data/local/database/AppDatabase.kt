package com.iptvplayer.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.iptvplayer.data.local.entities.ChannelEntity
import com.iptvplayer.data.local.entities.EpgProgrammeEntity
import com.iptvplayer.data.local.entities.PlaylistEntity

@Database(
    entities = [
        PlaylistEntity::class,
        ChannelEntity::class,
        EpgProgrammeEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao
    abstract fun channelDao(): ChannelDao
    abstract fun epgProgrammeDao(): EpgProgrammeDao
}
