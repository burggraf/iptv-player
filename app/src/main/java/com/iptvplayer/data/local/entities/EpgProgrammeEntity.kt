package com.iptvplayer.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "epg_programmes",
    indices = [Index("channelId"), Index("startAt"), Index("endAt")]
)
data class EpgProgrammeEntity(
    @PrimaryKey val id: String,
    val channelId: String,
    val title: String,
    val description: String?,
    val category: String?,
    val startAt: Long,  // epoch millis
    val endAt: Long,
    val iconUrl: String?,
)

fun EpgProgrammeEntity.toDomain() = com.iptvplayer.domain.model.EpgProgramme(
    id = id,
    channelId = channelId,
    title = title,
    description = description,
    category = category,
    startAt = java.time.Instant.ofEpochMilli(startAt),
    endAt = java.time.Instant.ofEpochMilli(endAt),
    iconUrl = iconUrl,
)

fun com.iptvplayer.domain.model.EpgProgramme.toEntity() = EpgProgrammeEntity(
    id = id,
    channelId = channelId,
    title = title,
    description = description,
    category = category,
    startAt = startAt.toEpochMilli(),
    endAt = endAt.toEpochMilli(),
    iconUrl = iconUrl,
)
