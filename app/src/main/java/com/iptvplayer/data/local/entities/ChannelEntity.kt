package com.iptvplayer.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "channels",
    indices = [Index("playlistId"), Index("group")]
)
data class ChannelEntity(
    @PrimaryKey val id: String,
    val playlistId: String,
    val number: String,
    val name: String,
    val logo: String?,
    val group: String?,
    val tvgId: String?,
    val streamUrl: String,
    val catchupDays: Int,
    val catchupSource: String?,
)

fun ChannelEntity.toDomain() = com.iptvplayer.domain.model.Channel(
    id = id,
    playlistId = playlistId,
    number = number,
    name = name,
    logo = logo,
    group = group,
    tvgId = tvgId,
    streamUrl = streamUrl,
    catchupDays = catchupDays,
    catchupSource = catchupSource,
)

fun com.iptvplayer.domain.model.Channel.toEntity() = ChannelEntity(
    id = id,
    playlistId = playlistId,
    number = number,
    name = name,
    logo = logo,
    group = group,
    tvgId = tvgId,
    streamUrl = streamUrl,
    catchupDays = catchupDays,
    catchupSource = catchupSource,
)
