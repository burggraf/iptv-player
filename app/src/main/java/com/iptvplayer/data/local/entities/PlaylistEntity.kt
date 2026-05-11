package com.iptvplayer.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.iptvplayer.domain.model.PlaylistType

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String,  // serialized PlaylistType enum name
    val url: String?,
    val username: String?,
    val password: String?,
    val serverUrl: String?,
    val addedAt: Long,  // epoch millis
    val lastUpdated: Long,
)

fun PlaylistEntity.toDomain() = com.iptvplayer.domain.model.Playlist(
    id = id,
    name = name,
    type = PlaylistType.valueOf(type),
    url = url,
    username = username,
    password = password,
    serverUrl = serverUrl,
    addedAt = java.time.Instant.ofEpochMilli(addedAt),
    lastUpdated = java.time.Instant.ofEpochMilli(lastUpdated),
)

fun com.iptvplayer.domain.model.Playlist.toEntity() = PlaylistEntity(
    id = id,
    name = name,
    type = type.name,
    url = url,
    username = username,
    password = password,
    serverUrl = serverUrl,
    addedAt = addedAt.toEpochMilli(),
    lastUpdated = lastUpdated.toEpochMilli(),
)
