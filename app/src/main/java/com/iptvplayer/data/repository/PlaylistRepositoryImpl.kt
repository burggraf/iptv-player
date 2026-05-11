package com.iptvplayer.data.repository

import com.iptvplayer.core.DispatcherProvider
import com.iptvplayer.core.AppResult
import com.iptvplayer.core.runCatchingSuspend
import com.iptvplayer.data.local.database.ChannelDao
import com.iptvplayer.data.local.database.PlaylistDao
import com.iptvplayer.data.local.entities.toDomain
import com.iptvplayer.data.local.entities.toEntity
import com.iptvplayer.data.parser.M3uParser
import io.ktor.client.HttpClient
import com.iptvplayer.domain.model.Channel
import com.iptvplayer.domain.model.Playlist
import com.iptvplayer.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class PlaylistRepositoryImpl(
    private val playlistDao: PlaylistDao,
    private val channelDao: ChannelDao,
    private val httpClient: HttpClient,
    private val m3uParser: M3uParser,
    private val dispatcherProvider: DispatcherProvider
) : PlaylistRepository {

    override suspend fun addPlaylist(playlist: Playlist): AppResult<Unit> = runCatchingSuspend {
        withContext(dispatcherProvider.io) {
            playlistDao.insert(playlist.toEntity())
        }
    }

    override suspend fun removePlaylist(id: String): AppResult<Unit> = runCatchingSuspend {
        withContext(dispatcherProvider.io) {
            playlistDao.delete(id)
            channelDao.deleteByPlaylist(id)
        }
    }

    override fun getPlaylists(): Flow<List<Playlist>> =
        playlistDao.getAll().map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun refreshPlaylist(id: String): AppResult<Unit> = runCatchingSuspend {
        // TODO: Fetch remote playlist, parse, save channels
        Unit
    }

    override fun getChannels(playlistId: String): Flow<List<Channel>> =
        channelDao.getChannelsByPlaylist(playlistId).map { entities ->
            entities.map { it.toDomain() }
        }
}
