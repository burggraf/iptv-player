package com.iptvplayer.presentation.viewmodel

import com.iptvplayer.core.AppResult
import com.iptvplayer.domain.model.Channel
import com.iptvplayer.domain.model.Playlist
import com.iptvplayer.domain.model.PlaylistType
import com.iptvplayer.domain.repository.PlaylistRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class PlaylistViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var playlistRepository: PlaylistRepository
    private lateinit var viewModel: PlaylistViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        playlistRepository = mockk(relaxed = true)
        every { playlistRepository.getPlaylists() } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is empty`() {
        viewModel = PlaylistViewModel(playlistRepository)
        val state = viewModel.uiState.value
        assertEquals(emptyList<Playlist>(), state.playlists)
        assertNull(state.selectedPlaylistId)
        assertNull(state.error)
    }

    @Test
    fun `add M3U playlist fetches channels and saves to database`() = runTest {
        every { playlistRepository.getPlaylists() } returns flowOf(emptyList())
        coEvery { playlistRepository.addPlaylist(any()) } returns AppResult.Success(Unit)

        viewModel = PlaylistViewModel(playlistRepository)

        viewModel.addPlaylist(
            name = "My Playlist",
            type = PlaylistType.M3U_URL,
            url = "http://example.com/playlist.m3u"
        )

        coVerify { playlistRepository.addPlaylist(match { it.name == "My Playlist" }) }
        assertEquals(null, viewModel.uiState.value.error)
        assertEquals("Playlist \"My Playlist\" added", viewModel.uiState.value.successMessage)
    }

    @Test
    fun `add Xtream playlist with auth credentials`() = runTest {
        every { playlistRepository.getPlaylists() } returns flowOf(emptyList())
        coEvery { playlistRepository.addPlaylist(any()) } returns AppResult.Success(Unit)

        viewModel = PlaylistViewModel(playlistRepository)

        viewModel.addPlaylist(
            name = "My Xtream",
            type = PlaylistType.XTREAM,
            serverUrl = "http://example.com",
            username = "user",
            password = "pass"
        )

        coVerify {
            playlistRepository.addPlaylist(match {
                it.type == PlaylistType.XTREAM &&
                    it.username == "user" &&
                    it.password == "pass" &&
                    it.serverUrl == "http://example.com"
            })
        }
    }

    @Test
    fun `xtream authentication failure shows error`() = runTest {
        every { playlistRepository.getPlaylists() } returns flowOf(emptyList())
        coEvery { playlistRepository.addPlaylist(any()) } returns AppResult.Error(
            Exception("Authentication failed")
        )

        viewModel = PlaylistViewModel(playlistRepository)

        viewModel.addPlaylist(
            name = "Bad Xtream",
            type = PlaylistType.XTREAM,
            serverUrl = "http://bad.com",
            username = "wrong",
            password = "wrong"
        )

        assertEquals("Authentication failed", viewModel.uiState.value.error)
    }

    @Test
    fun `remove playlist deletes from repository`() = runTest {
        val testPlaylist = testPlaylist("p1")
        every { playlistRepository.getPlaylists() } returns flowOf(listOf(testPlaylist))
        coEvery { playlistRepository.removePlaylist("p1") } returns AppResult.Success(Unit)

        viewModel = PlaylistViewModel(playlistRepository)
        viewModel.removePlaylist("p1")

        coVerify { playlistRepository.removePlaylist("p1") }
    }

    @Test
    fun `refresh playlist calls repository`() = runTest {
        every { playlistRepository.getPlaylists() } returns flowOf(emptyList())
        coEvery { playlistRepository.refreshPlaylist("p1") } returns AppResult.Success(Unit)

        viewModel = PlaylistViewModel(playlistRepository)
        viewModel.refreshPlaylist("p1")

        coVerify { playlistRepository.refreshPlaylist("p1") }
        assertEquals("Playlist refreshed", viewModel.uiState.value.successMessage)
    }

    @Test
    fun `select playlist updates selectedPlaylistId`() = runTest {
        every { playlistRepository.getPlaylists() } returns flowOf(emptyList())
        viewModel = PlaylistViewModel(playlistRepository)

        viewModel.selectPlaylist("p1")

        assertEquals("p1", viewModel.uiState.value.selectedPlaylistId)
    }

    @Test
    fun `load playlists emits from repository flow`() = runTest {
        val playlists = listOf(testPlaylist("p1"), testPlaylist("p2"))
        every { playlistRepository.getPlaylists() } returns flowOf(playlists)

        viewModel = PlaylistViewModel(playlistRepository)

        assertEquals(2, viewModel.uiState.value.playlists.size)
        assertEquals("p1", viewModel.uiState.value.selectedPlaylistId)
    }

    @Test
    fun `clear messages resets error and successMessage`() = runTest {
        every { playlistRepository.getPlaylists() } returns flowOf(emptyList())
        coEvery { playlistRepository.addPlaylist(any()) } returns AppResult.Error(
            Exception("Network error")
        )

        viewModel = PlaylistViewModel(playlistRepository)
        viewModel.addPlaylist("Test", PlaylistType.M3U_URL, "http://test.com")
        assertEquals("Network error", viewModel.uiState.value.error)

        viewModel.clearMessages()
        assertNull(viewModel.uiState.value.error)
        assertNull(viewModel.uiState.value.successMessage)
    }

    private fun testPlaylist(id: String) = Playlist(
        id = id,
        name = "Playlist $id",
        type = PlaylistType.M3U_URL,
        url = "http://example.com/$id.m3u",
        addedAt = Instant.now(),
        lastUpdated = Instant.now(),
    )
}
