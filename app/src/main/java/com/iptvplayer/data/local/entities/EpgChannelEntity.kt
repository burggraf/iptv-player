package com.iptvplayer.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "epg_channels",
    indices = [Index("channelId", unique = true)]
)
data class EpgChannelEntity(
    @PrimaryKey val id: String,
    val channelId: String,  // matches Channel.tvgId
    val displayName: String,
    val iconUrl: String?,
)

fun EpgChannelEntity.toDomain() = com.iptvplayer.domain.model.EpgChannel(
    id = id,
    displayName = displayName,
    iconUrl = iconUrl,
    programmes = emptyList(),
)

fun com.iptvplayer.domain.model.EpgChannel.toEntity() = EpgChannelEntity(
    id = id,
    channelId = id,
    displayName = displayName,
    iconUrl = iconUrl,
)
