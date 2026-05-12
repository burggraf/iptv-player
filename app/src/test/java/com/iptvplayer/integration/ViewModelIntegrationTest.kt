package com.iptvplayer.integration

import com.iptvplayer.core.AppResult
import com.iptvplayer.domain.model.Channel
import com.iptvplayer.domain.model.EpgProgramme
import com.iptvplayer.domain.model.PlaybackState
import com.iptvplayer.domain.model.Playlist
import com.iptvplayer.domain.model.PlaylistType
import com.iptvplayer.domain.repository.EpgRepository
import com.iptvplayer.domain.repository.PlaybackRepository
import com.iptvplayer.domain.repository.PlaylistRepository
import com.iptvplayer.domain.usecase.FetchEpgUseCase
import com.iptvplayer.domain.usecase.LoadPlaylistUseCase
import com.iptvplayer.domain.usecase.PlayChannelUseCase
import com.iptvplayer.presentation.viewmodel.EpgViewModel
import com.iptvplayer.presentation.viewmodel.PlayerViewModel
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
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.Instant

/**
 * Phase 7 — Integration Tests.
 * ViewModel + UseCase + Repository interaction tests.
 * Verifies full call chain, not just single component behavior.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelIntegrationTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var playlistRepo: PlaylistRepository
    private lateinit var epgRepo: EpgRepository
    private lateinit var playbackRepo: PlaybackRepository
    private lateinit var loadPlaylistUseCase: LoadPlaylistUseCase
    private lateinit var fetchEpgUseCase: FetchEpgUseCase
    private lateinit var playChannelUseCase: PlayChannelUseCase

    private val testChannels = listOf(
        Channel(id = "ch1", playlistId = "p1", number = "1", name = "BBC One", group = "UK", streamUrl = "http://stream1"),
        Channel(id = "ch2", playlistId = "p1", number = "2", name = "BBC Two", group = "UK", streamUrl = "http://stream2"),
        Channel(id = "ch3", playlistId = "p1", number = "3", name = "Sky News", group = "News", streamUrl = "http://stream3"),
    )

    private val testPlaylist = Playlist(
        id = "p1",
        name = "Test Playlist",
        type = PlaylistType.M3U_URL,
        url = "http://example.com/playlist.m3u",
        channels = testChannels,
        addedAt = Instant.now(),
        lastUpdated = Instant.now(),
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        playlistRepo = mockk(relaxed = true)
        epgRepo = mockk(relaxed = true)
        playbackRepo = mockk(relaxed = true)

        loadPlaylistUseCase = LoadPlaylistUseCase(playlistRepo)
        fetchEpgUseCase = FetchEpgUseCase(epgRepo)
        playChannelUseCase = PlayChannelUseCase(playbackRepo)

        every { playbackRepo.getPlaybackState() } returns flowOf(PlaybackState.Idle)
        every { playbackRepo.getPlayer() } returns mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── Playlist → EPG Full Flow ───────────────────────────────────────────

    @Test
    fun `load playlist saves to repository and succeeds`() = runTest {
        // When: Load playlist through use case
        val result = loadPlaylistUseCase(testPlaylist)

        // Then: Success
        assertTrue(result is AppResult.Success)
        coVerify { playlistRepo.addPlaylist(testPlaylist) }
    }

    @Test
    fun `playlist load failure propagates error`() = runTest {
        coEvery { playlistRepo.addPlaylist(any()) } throws RuntimeException("Network error")

        val result = loadPlaylistUseCase(testPlaylist)

        assertTrue(result is AppResult.Error)
        assertEquals("Network error", (result as AppResult.Error).exception.message)
    }

    @Test
    fun `fetch EPG propagates through use case`() = runTest {
        coEvery { epgRepo.fetchAndCacheEpg(any()) } returns AppResult.Success(Unit)

        val result = fetchEpgUseCase(listOf("p1"))

        assertTrue(result is AppResult.Success)
        coVerify { epgRepo.fetchAndCacheEpg(listOf("p1")) }
    }

    @Test
    fun `fetch EPG failure propagates error`() = runTest {
        coEvery { epgRepo.fetchAndCacheEpg(listOf("p1")) } throws RuntimeException("EPG server timeout")

        val result = fetchEpgUseCase(listOf("p1"))

        assertTrue(result is AppResult.Error)
    }

    // ── Player + EPG Combined Flow ─────────────────────────────────────────

    @Test
    fun `select channel then get now playing works end-to-end`() = runTest {
        // Given: Player with channels
        val viewModel = PlayerViewModel(playbackRepo, playChannelUseCase)
        viewModel.setChannels(testChannels)

        // When: Select a channel
        viewModel.selectChannel(testChannels[0])

        // Then: Channel selected
        assertEquals(testChannels[0], viewModel.currentChannel.value)
        coVerify { playChannelUseCase(testChannels[0]) }

        // Given: EPG data available
        val epgViewModel = EpgViewModel(fetchEpgUseCase)
        val now = Instant.now()
        epgViewModel.setProgrammes(
            mapOf(
                "ch1" to listOf(
                    EpgProgramme(id = "p1", channelId = "ch1", title = "Current Show",
                        startAt = now.minusSeconds(1800), endAt = now.plusSeconds(1800)),
                )
            )
        )

        // When: Get now playing for current channel
        val nowPlaying = epgViewModel.uiState.value.getNowPlaying("ch1")

        // Then: Returns current programme
        assertNotNull(nowPlaying)
        assertEquals("Current Show", nowPlaying?.title)
    }

    // ── Channel Switch Flow ────────────────────────────────────────────────

    @Test
    fun `channel switch triggers play channel use case`() = runTest {
        every { playbackRepo.getPlaybackState() } returns flowOf(PlaybackState.Loading, PlaybackState.Playing(0, 0))

        val viewModel = PlayerViewModel(playbackRepo, playChannelUseCase)
        viewModel.setChannels(testChannels)

        viewModel.selectChannel(testChannels[0])
        viewModel.switchChannel(1) // Next

        assertEquals(testChannels[1], viewModel.currentChannel.value)
        coVerify {
            playChannelUseCase(testChannels[0])
            playChannelUseCase(testChannels[1])
        }
    }

    // ── Group Filter + Search Integration ──────────────────────────────────

    @Test
    fun `group filter combined with search narrows results`() = runTest {
        val viewModel = EpgViewModel(fetchEpgUseCase)
        viewModel.loadChannels(testChannels)

        // Filter to News group
        viewModel.selectGroup("News")

        val filtered = viewModel.uiState.value.filteredChannels
        assertEquals(1, filtered.size)
        assertEquals("Sky News", filtered[0].name)

        // Now search within that group
        viewModel.setSearchQuery("sky")
        // Allow debounce to process (UnconfinedTestDispatcher executes immediately)
        val refined = viewModel.uiState.value.filteredChannels
        assertEquals(1, refined.size)
        assertEquals("Sky News", refined[0].name)
    }

    // ── EPG Fetch Error Recovery ───────────────────────────────────────────

    @Test
    fun `epg fetch failure does not clear existing data`() = runTest {
        coEvery { epgRepo.fetchAndCacheEpg(listOf("p1")) } throws RuntimeException("EPG server timeout")

        val viewModel = EpgViewModel(fetchEpgUseCase)

        // Set some programmes first
        val now = Instant.now()
        viewModel.setProgrammes(
            mapOf("ch1" to listOf(
                EpgProgramme(id = "p1", channelId = "ch1", title = "Cached Show",
                    startAt = now.minusSeconds(1800), endAt = now.plusSeconds(1800)),
            ))
        )

        // Fetch fails
        viewModel.fetchEpg(listOf("p1"))

        // Error is recorded but existing programmes still there
        assertEquals("EPG server timeout", viewModel.uiState.value.error)
        assertEquals(1, viewModel.uiState.value.programmes["ch1"]?.size)
        assertEquals("Cached Show", viewModel.uiState.value.programmes["ch1"]!![0].title)
    }
}
