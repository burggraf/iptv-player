package com.iptvplayer.data.repository

import com.iptvplayer.core.AppResult
import com.iptvplayer.core.runCatchingSuspend
import com.iptvplayer.data.local.database.ChannelDao
import com.iptvplayer.data.local.database.PlaylistDao
import com.iptvplayer.data.local.entities.toEntity
import com.iptvplayer.data.local.entities.toDomain
import com.iptvplayer.data.parser.M3uParser
import com.iptvplayer.data.parser.XtreamParser
import com.iptvplayer.data.remote.PlaylistApi
import com.iptvplayer.core.DispatcherProvider
import com.iptvplayer.domain.model.Channel
import com.iptvplayer.domain.model.Playlist
import com.iptvplayer.domain.model.PlaylistType
import com.iptvplayer.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class PlaylistRepositoryImpl(
    private val playlistDao: PlaylistDao,
    private val channelDao: ChannelDao,
    private val playlistApi: PlaylistApi,
    private val m3uParser: M3uParser,
    private val xtreamParser: XtreamParser,
    private val dispatcherProvider: DispatcherProvider
) : PlaylistRepository {

    override suspend fun addPlaylist(playlist: Playlist): AppResult<Unit> = runCatchingSuspend {
        withContext(dispatcherProvider.io) {
            playlistDao.insert(playlist.toEntity())
            android.util.Log.d("PlaylistRepo", "Playlist inserted: ${playlist.name}")

            val channels = when (playlist.type) {
                PlaylistType.M3U_URL -> {
                    playlist.url?.let { fetchAndParseM3u(it, playlist.id) } ?: emptyList()
                }
                PlaylistType.XTREAM -> {
                    playlist.serverUrl?.let { serverUrl ->
                        android.util.Log.d("PlaylistRepo", "Fetching Xtream from $serverUrl")
                        fetchAndParseXtream(
                            serverUrl = serverUrl,
                            username = playlist.username ?: "",
                            password = playlist.password ?: "",
                            playlistId = playlist.id
                        )
                    } ?: emptyList()
                }
                PlaylistType.M3U_FILE -> emptyList() // handled by caller
            }

            android.util.Log.d("PlaylistRepo", "Fetched ${channels.size} channels for ${playlist.name}")

            if (channels.isNotEmpty()) {
                channelDao.insertAll(channels.map { it.toEntity() })
                android.util.Log.d("PlaylistRepo", "Inserted ${channels.size} channels into DB")
            }
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
        withContext(dispatcherProvider.io) {
            val entity = playlistDao.getById(id)
            if (entity != null) {
                val playlist = entity.toDomain()
                val channels = when (playlist.type) {
                    PlaylistType.M3U_URL -> {
                        playlist.url?.let { fetchAndParseM3u(it, playlist.id) } ?: emptyList()
                    }
                    PlaylistType.XTREAM -> {
                        playlist.serverUrl?.let { serverUrl ->
                            fetchAndParseXtream(
                                serverUrl = serverUrl,
                                username = playlist.username ?: "",
                                password = playlist.password ?: "",
                                playlistId = playlist.id
                            )
                        } ?: emptyList()
                    }
                    PlaylistType.M3U_FILE -> emptyList()
                }

                if (channels.isNotEmpty()) {
                    channelDao.deleteByPlaylist(playlist.id)
                    channelDao.insertAll(channels.map { it.toEntity() })
                }
            }
        }
    }

    override fun getChannels(playlistId: String): Flow<List<Channel>> =
        channelDao.getChannelsByPlaylist(playlistId).map { entities ->
            entities.map { it.toDomain() }
        }

    private suspend fun fetchAndParseM3u(url: String, playlistId: String): List<Channel> {
        val raw = playlistApi.fetchM3u(url)
        return m3uParser.parse(raw).map { it.copy(playlistId = playlistId) }
    }

    private suspend fun fetchAndParseXtream(
        serverUrl: String,
        username: String,
        password: String,
        playlistId: String
    ): List<Channel> {
        val authRaw = playlistApi.xtreamAuthRaw(serverUrl, username, password)
        val auth = xtreamParser.parseAuth(authRaw)
        if (!auth.success) throw IllegalStateException("Xtream auth failed: ${auth.userInfo?.status}")

        val rawStreams = playlistApi.xtreamLiveStreams(serverUrl, username, password)
        return xtreamParser.parseStreams(rawStreams, playlistId = playlistId, serverUrl = serverUrl)
    }
}
