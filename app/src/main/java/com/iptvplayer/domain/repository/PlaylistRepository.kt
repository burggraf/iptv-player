package com.iptvplayer.domain.repository

import com.iptvplayer.core.AppResult
import com.iptvplayer.domain.model.Channel
import com.iptvplayer.domain.model.Playlist
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository {
    suspend fun addPlaylist(playlist: Playlist): AppResult<Unit>
    suspend fun removePlaylist(id: String): AppResult<Unit>
    fun getPlaylists(): Flow<List<Playlist>>
    suspend fun refreshPlaylist(id: String): AppResult<Unit>
    fun getChannels(playlistId: String): Flow<List<Channel>>
}
