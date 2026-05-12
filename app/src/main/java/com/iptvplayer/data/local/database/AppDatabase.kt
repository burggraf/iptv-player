package com.iptvplayer.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.iptvplayer.data.local.entities.ChannelEntity
import com.iptvplayer.data.local.entities.EpgChannelEntity
import com.iptvplayer.data.local.entities.EpgProgrammeEntity
import com.iptvplayer.data.local.entities.FavoriteChannelEntity
import com.iptvplayer.data.local.entities.PlaylistEntity

@Database(
    entities = [
        PlaylistEntity::class,
        ChannelEntity::class,
        EpgChannelEntity::class,
        EpgProgrammeEntity::class,
        FavoriteChannelEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao
    abstract fun channelDao(): ChannelDao
    abstract fun epgChannelDao(): EpgChannelDao
    abstract fun epgProgrammeDao(): EpgProgrammeDao
    abstract fun favoriteDao(): FavoriteDao
}
