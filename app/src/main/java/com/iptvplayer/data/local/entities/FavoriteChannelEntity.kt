package com.iptvplayer.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteChannelEntity(
    @PrimaryKey val channelId: String,
    val addedAt: Long,  // epoch millis
)
