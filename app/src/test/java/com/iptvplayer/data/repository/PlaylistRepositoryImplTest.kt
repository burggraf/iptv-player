package com.iptvplayer.data.repository

import com.iptvplayer.core.DispatcherProvider
import com.iptvplayer.data.local.database.ChannelDao
import com.iptvplayer.data.local.database.PlaylistDao
import com.iptvplayer.data.local.entities.ChannelEntity
import com.iptvplayer.data.local.entities.PlaylistEntity
import com.iptvplayer.data.parser.M3uParser
import com.iptvplayer.data.parser.XtreamParser
import com.iptvplayer.data.remote.PlaylistApi
import com.iptvplayer.core.AppResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PlaylistRepositoryImplTest {

    private val playlistDao: PlaylistDao = mockk(relaxed = true)
    private val channelDao: ChannelDao = mockk(relaxed = true)
    private val playlistApi: PlaylistApi = mockk()
    private val m3uParser = M3uParser()
    private val xtreamParser = XtreamParser()
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var repository: PlaylistRepositoryImpl

    @Before
    fun setup() {
        repository = PlaylistRepositoryImpl(
            playlistDao = playlistDao,
            channelDao = channelDao,
            playlistApi = playlistApi,
            m3uParser = m3uParser,
            xtreamParser = xtreamParser,
            dispatcherProvider = DispatcherProvider(
                io = testDispatcher,
                default = testDispatcher,
                main = testDispatcher
            )
        )
    }

    @Test
    fun `add M3U playlist saves to database`() = runTest {
        coEvery { playlistApi.fetchM3u(any()) } returns TEST_M3U
        coEvery { channelDao.insertAll(any()) } returns Unit

        val playlist = testPlaylist()

        repository.addPlaylist(playlist)

        coVerify { playlistDao.insert(any()) }
        coVerify { channelDao.insertAll(match { it.size == 2 }) }
    }

    @Test
    fun `remove playlist deletes playlist and channels`() = runTest {
        repository.removePlaylist("test-1")

        coVerify { playlistDao.delete("test-1") }
        coVerify { channelDao.deleteByPlaylist("test-1") }
    }

    @Test
    fun `get playlists emits flow`() = runTest {
        val entity = PlaylistEntity(
            id = "p1", name = "Test", type = "M3U_URL",
            url = "http://example.com", username = null, password = null,
            serverUrl = null, addedAt = 1000L, lastUpdated = 1000L
        )
        every { playlistDao.getAll() } returns flowOf(listOf(entity))

        val playlists = repository.getPlaylists()

        assertEquals("Test", playlists.first()[0].name)
    }

    @Test
    fun `get channels emits flow`() = runTest {
        val entity = ChannelEntity(
            id = "c1", playlistId = "p1", number = "1",
            name = "BBC One", logo = null, group = "UK",
            tvgId = "BBC1.uk", streamUrl = "http://example.com",
            catchupDays = 0, catchupSource = null
        )
        every { channelDao.getChannelsByPlaylist("p1") } returns flowOf(listOf(entity))

        val channels = repository.getChannels("p1")

        assertEquals("BBC One", channels.first()[0].name)
    }

    @Test
    fun `refresh playlist re-fetches and replaces channels`() = runTest {
        val entity = PlaylistEntity(
            id = "p1", name = "Test", type = "M3U_URL",
            url = "http://example.com", username = null, password = null,
            serverUrl = null, addedAt = 1000L, lastUpdated = 1000L
        )
        coEvery { playlistDao.getById("p1") } returns entity
        coEvery { playlistApi.fetchM3u(any()) } returns TEST_M3U
        coEvery { channelDao.insertAll(any()) } returns Unit

        repository.refreshPlaylist("p1")

        coVerify { channelDao.deleteByPlaylist("p1") }
        coVerify { channelDao.insertAll(match { it.size == 2 }) }
    }

    private fun testPlaylist() = com.iptvplayer.domain.model.Playlist(
        id = "test-1",
        name = "Test",
        type = com.iptvplayer.domain.model.PlaylistType.M3U_URL,
        url = "http://example.com/playlist.m3u",
    )

    companion object {
        private const val TEST_M3U = """
#EXTM3U
#EXTINF:-1 tvg-id="BBC1.uk" tvg-chno="1" tvg-name="BBC One" group-title="UK",BBC One HD
http://example.com/bbc1
#EXTINF:-1 tvg-id="ITV1.uk" tvg-chno="3" tvg-name="ITV" group-title="UK",ITV HD
http://example.com/itv
"""
    }
}
