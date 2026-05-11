package com.iptvplayer.domain.repository

import com.iptvplayer.core.Result
import com.iptvplayer.domain.model.Channel
import com.iptvplayer.domain.model.Playlist
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository {
    suspend fun addPlaylist(playlist: Playlist): Result<Unit>
    suspend fun removePlaylist(id: String): Result<Unit>
    fun getPlaylists(): Flow<List<Playlist>>
    suspend fun refreshPlaylist(id: String): Result<Unit>
    fun getChannels(playlistId: String): Flow<List<Channel>>
}
