package com.iptvplayer.presentation.viewmodel

import com.iptvplayer.core.AppResult
import com.iptvplayer.domain.model.Channel
import com.iptvplayer.domain.model.EpgProgramme
import com.iptvplayer.domain.usecase.FetchEpgUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class EpgViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fetchEpgUseCase: FetchEpgUseCase
    private lateinit var viewModel: EpgViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fetchEpgUseCase = mockk()
        coEvery { fetchEpgUseCase(any()) } returns AppResult.Success(Unit)
        viewModel = EpgViewModel(fetchEpgUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has empty channels and programmes`() {
        val state = viewModel.uiState.value
        assertEquals(emptyList<Channel>(), state.channels)
        assertEquals(emptyMap<String, List<EpgProgramme>>(), state.programmes)
        assertEquals(EpgUiState.GROUP_ALL, state.selectedGroup)
        assertEquals("", state.searchQuery)
        assertNull(state.error)
    }

    @Test
    fun `load channels sets channels and extracts groups`() {
        val channels = listOf(
            Channel(id = "1", playlistId = "p1", number = "1", name = "BBC One", group = "Entertainment", streamUrl = "http://..."),
            Channel(id = "2", playlistId = "p1", number = "2", name = "BBC Two", group = "Entertainment", streamUrl = "http://..."),
            Channel(id = "3", playlistId = "p1", number = "3", name = "Sky News", group = "News", streamUrl = "http://..."),
            Channel(id = "4", playlistId = "p1", number = "4", name = "CNN", streamUrl = "http://..."),
        )

        viewModel.loadChannels(channels)

        val state = viewModel.uiState.value
        assertEquals(4, state.channels.size)
        assertEquals(listOf("All", "Entertainment", "News"), state.groups)
    }

    @Test
    fun `filtered channels returns all when group is All`() {
        viewModel.loadChannels(testChannels())
        viewModel.selectGroup(EpgUiState.GROUP_ALL)

        val filtered = viewModel.uiState.value.filteredChannels
        assertEquals(4, filtered.size)
    }

    @Test
    fun `filtered channels filters by selected group`() {
        viewModel.loadChannels(testChannels())
        viewModel.selectGroup("News")

        val filtered = viewModel.uiState.value.filteredChannels
        assertEquals(2, filtered.size)
        assertTrue(filtered.all { it.group == "News" })
    }

    @Test
    fun `filtered channels filters by search query`() {
        viewModel.loadChannels(testChannels())
        viewModel.updateSearchQuery("bbc")

        val filtered = viewModel.uiState.value.filteredChannels
        assertEquals(2, filtered.size)
        assertTrue(filtered.all { it.name.contains("BBC", ignoreCase = true) })
    }

    @Test
    fun `filtered channels combines group and search filters`() {
        viewModel.loadChannels(testChannels())
        viewModel.selectGroup("Entertainment")
        viewModel.updateSearchQuery("two")

        val filtered = viewModel.uiState.value.filteredChannels
        assertEquals(1, filtered.size)
        assertEquals("BBC Two", filtered[0].name)
    }

    @Test
    fun `getNowPlaying returns current programme`() {
        val now = Instant.now()
        val currentProgramme = EpgProgramme(
            id = "p1", channelId = "1", title = "News at Noon",
            startAt = now.minusSeconds(1800), endAt = now.plusSeconds(1800)
        )
        val futureProgramme = EpgProgramme(
            id = "p2", channelId = "1", title = "Afternoon Show",
            startAt = now.plusSeconds(3600), endAt = now.plusSeconds(7200)
        )

        viewModel.setProgrammes(mapOf("1" to listOf(currentProgramme, futureProgramme)))

        val playing = viewModel.uiState.value.getNowPlaying("1")
        assertEquals("News at Noon", playing?.title)
    }

    @Test
    fun `getNowPlaying returns null when no current programme`() {
        val past = Instant.now().minusSeconds(7200)
        val future = Instant.now().plusSeconds(3600)
        val pastProgramme = EpgProgramme(
            id = "p1", channelId = "1", title = "Old News",
            startAt = past.minusSeconds(3600), endAt = past
        )
        val futureProgramme = EpgProgramme(
            id = "p2", channelId = "1", title = "Future Show",
            startAt = future, endAt = future.plusSeconds(3600)
        )

        viewModel.setProgrammes(mapOf("1" to listOf(pastProgramme, futureProgramme)))

        val playing = viewModel.uiState.value.getNowPlaying("1")
        assertNull(playing)
    }

    @Test
    fun `getUpcoming returns future programmes`() {
        val now = Instant.now()
        val upcoming1 = EpgProgramme(
            id = "p1", channelId = "1", title = "Next Show",
            startAt = now.plusSeconds(600), endAt = now.plusSeconds(2400)
        )
        val upcoming2 = EpgProgramme(
            id = "p2", channelId = "1", title = "Later Show",
            startAt = now.plusSeconds(3600), endAt = now.plusSeconds(5400)
        )

        viewModel.setProgrammes(mapOf("1" to listOf(upcoming1, upcoming2)))

        val upcoming = viewModel.uiState.value.getUpcoming("1")
        assertEquals(2, upcoming.size)
        assertEquals("Next Show", upcoming[0].title)
    }

    @Test
    fun `getUpcoming limits to requested count`() {
        val now = Instant.now()
        val programmes = (1..10).map { i ->
            EpgProgramme(
                id = "p$i", channelId = "1", title = "Show $i",
                startAt = now.plusSeconds(i * 3600L), endAt = now.plusSeconds((i + 1) * 3600L)
            )
        }

        viewModel.setProgrammes(mapOf("1" to programmes))

        val upcoming = viewModel.uiState.value.getUpcoming("1", count = 3)
        assertEquals(3, upcoming.size)
    }

    @Test
    fun `fetchEpg sets loading then success`() = runTest {
        coEvery { fetchEpgUseCase(any()) } returns AppResult.Success(Unit)

        viewModel.fetchEpg(listOf("p1"))

        assertEquals(false, viewModel.uiState.value.isLoading)
        coVerify { fetchEpgUseCase(listOf("p1")) }
    }

    @Test
    fun `fetchEpg sets error on failure`() = runTest {
        coEvery { fetchEpgUseCase(any()) } returns AppResult.Error(Exception("Network error"))

        viewModel.fetchEpg(listOf("p1"))

        assertEquals("Network error", viewModel.uiState.value.error)
    }

    @Test
    fun `set programmes updates state`() {
        val programmes = mapOf(
            "1" to listOf(
                EpgProgramme(id = "p1", channelId = "1", title = "Morning News",
                    startAt = Instant.now().minusSeconds(3600), endAt = Instant.now())
            )
        )

        viewModel.setProgrammes(programmes)

        assertEquals(1, viewModel.uiState.value.programmes["1"]?.size)
    }

    private fun testChannels() = listOf(
        Channel(id = "1", playlistId = "p1", number = "1", name = "BBC One", group = "Entertainment", streamUrl = "http://..."),
        Channel(id = "2", playlistId = "p1", number = "2", name = "BBC Two", group = "Entertainment", streamUrl = "http://..."),
        Channel(id = "3", playlistId = "p1", number = "3", name = "Sky News", group = "News", streamUrl = "http://..."),
        Channel(id = "4", playlistId = "p1", number = "4", name = "CNN", group = "News", streamUrl = "http://..."),
    )
}
