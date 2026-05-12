package com.iptvplayer.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.iptvplayer.core.AppResult
import com.iptvplayer.domain.model.Channel
import com.iptvplayer.domain.model.PlaybackState
import com.iptvplayer.domain.repository.PlaybackRepository
import com.iptvplayer.domain.usecase.PlayChannelUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val repositoryPlaybackState = MutableStateFlow<PlaybackState>(PlaybackState.Idle)
    private lateinit var repository: PlaybackRepository
    private lateinit var playChannelUseCase: PlayChannelUseCase
    private lateinit var viewModel: PlayerViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        repository = mockk()
        every { repository.getPlaybackState() } returns repositoryPlaybackState
        every { repository.getPlayer() } returns mockk()
        coEvery { repository.playChannel(any()) } returns AppResult.Success(Unit)
        coEvery { repository.releasePlayer() } returns Unit

        playChannelUseCase = PlayChannelUseCase(repository)
        viewModel = PlayerViewModel(repository, playChannelUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is idle with no channel`() {
        assertEquals(PlaybackState.Idle, viewModel.playbackState.value)
        assertNull(viewModel.currentChannel.value)
        assertFalse(viewModel.isFullscreen.value)
    }

    @Test
    fun `selecting channel updates current channel`() = runTest {
        val channel = testChannel()

        viewModel.selectChannel(channel)

        assertEquals(channel, viewModel.currentChannel.value)
    }

    @Test
    fun `selecting channel calls use case`() = runTest {
        val channel = testChannel()

        viewModel.selectChannel(channel)

        coVerify { playChannelUseCase(channel) }
    }

    @Test
    fun `set fullscreen toggles fullscreen state`() {
        viewModel.setFullscreen(true)
        assertTrue(viewModel.isFullscreen.value)

        viewModel.setFullscreen(false)
        assertFalse(viewModel.isFullscreen.value)
    }

    @Test
    fun `toggle fullscreen flips state`() {
        assertFalse(viewModel.isFullscreen.value)

        viewModel.toggleFullscreen()
        assertTrue(viewModel.isFullscreen.value)

        viewModel.toggleFullscreen()
        assertFalse(viewModel.isFullscreen.value)
    }

    @Test
    fun `player returns repository player`() {
        val mockPlayer = mockk<androidx.media3.common.Player>()
        every { repository.getPlayer() } returns mockPlayer

        assertEquals(mockPlayer, viewModel.player)
    }

    @Test
    fun `onCleared releases player`() {
        // onCleared is protected — verify via reflection
        val onCleared = ViewModel::class.java.getDeclaredMethod("onCleared")
        onCleared.isAccessible = true
        onCleared.invoke(viewModel)

        coVerify { repository.releasePlayer() }
    }

    @Test
    fun `playback error sets error state via repository flow`() = runTest {
        // Simulate error from repository
        repositoryPlaybackState.value = PlaybackState.Error("Connection refused", recoverable = true)

        val state = viewModel.playbackState.value as PlaybackState.Error
        assertEquals("Connection refused", state.message)
        assertTrue(state.recoverable)
    }

    @Test
    fun `playback loading state propagates`() = runTest {
        repositoryPlaybackState.value = PlaybackState.Loading

        assertEquals(PlaybackState.Loading, viewModel.playbackState.value)
    }

    @Test
    fun `playback buffering state propagates`() = runTest {
        repositoryPlaybackState.value = PlaybackState.Buffering

        assertEquals(PlaybackState.Buffering, viewModel.playbackState.value)
    }

    private fun testChannel() = Channel(
        id = "1",
        playlistId = "pl-1",
        number = "1",
        name = "BBC One",
        logo = "http://example.com/logo.png",
        group = "UK",
        tvgId = "BBC1.uk",
        streamUrl = "http://example.com/stream.m3u8"
    )
}
