package com.iptvplayer.presentation.viewmodel

import com.iptvplayer.core.AppResult
import com.iptvplayer.domain.repository.FavoritesRepository
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
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var favoritesRepository: FavoritesRepository
    private lateinit var viewModel: FavoritesViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        favoritesRepository = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads favorites from repository`() {
        val favIds = listOf("ch1", "ch2", "ch3")
        every { favoritesRepository.getFavorites() } returns flowOf(favIds)

        viewModel = FavoritesViewModel(favoritesRepository)

        assertEquals(favIds, viewModel.uiState.value.favorites)
    }

    @Test
    fun `initial state is empty when no favorites`() {
        every { favoritesRepository.getFavorites() } returns flowOf(emptyList())

        viewModel = FavoritesViewModel(favoritesRepository)

        assertEquals(emptyList<String>(), viewModel.uiState.value.favorites)
    }

    @Test
    fun `toggle favorite calls repository`() = runTest {
        every { favoritesRepository.getFavorites() } returns flowOf(emptyList())
        coEvery { favoritesRepository.toggleFavorite("ch1") } returns AppResult.Success(Unit)

        viewModel = FavoritesViewModel(favoritesRepository)
        viewModel.toggleFavorite("ch1")

        coVerify { favoritesRepository.toggleFavorite("ch1") }
    }

    @Test
    fun `toggle favorite error is captured`() = runTest {
        every { favoritesRepository.getFavorites() } returns flowOf(emptyList())
        coEvery { favoritesRepository.toggleFavorite("ch1") } returns AppResult.Error(
            Exception("DB error")
        )

        viewModel = FavoritesViewModel(favoritesRepository)
        viewModel.toggleFavorite("ch1")

        assertEquals("DB error", viewModel.uiState.value.error)
    }

    @Test
    fun `clear error resets error state`() = runTest {
        every { favoritesRepository.getFavorites() } returns flowOf(emptyList())
        coEvery { favoritesRepository.toggleFavorite("ch1") } returns AppResult.Error(
            Exception("DB error")
        )

        viewModel = FavoritesViewModel(favoritesRepository)
        viewModel.toggleFavorite("ch1")
        assertEquals("DB error", viewModel.uiState.value.error)

        viewModel.clearError()
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `isFavorite returns flow from repository`() {
        every { favoritesRepository.getFavorites() } returns flowOf(emptyList())
        every { favoritesRepository.isFavorite("ch1") } returns flowOf(true)

        viewModel = FavoritesViewModel(favoritesRepository)

        // Just verify the call doesn't crash
        val flow = viewModel.isFavorite("ch1")
        assert(flow != null)
    }
}
