package com.iptvplayer.domain.model

import java.time.Instant

enum class PlaylistType {
    M3U_URL,
    M3U_FILE,
    XTREAM
}

data class Playlist(
    val id: String,
    val name: String,
    val type: PlaylistType,
    val url: String? = null,
    val username: String? = null,
    val password: String? = null,
    val serverUrl: String? = null,
    val channels: List<Channel> = emptyList(),
    val addedAt: Instant = Instant.now(),
    val lastUpdated: Instant = Instant.now(),
)
