package com.iptvplayer.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptvplayer.core.AppResult
import com.iptvplayer.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class FavoritesUiState(
    val favorites: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

class FavoritesViewModel(
    private val favoritesRepository: FavoritesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    init {
        favoritesRepository.getFavorites()
            .onEach { ids ->
                _uiState.value = _uiState.value.copy(favorites = ids, isLoading = false)
            }
            .launchIn(viewModelScope)
    }

    fun toggleFavorite(channelId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            when (val result = favoritesRepository.toggleFavorite(channelId)) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
                is AppResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.exception.message ?: "Failed to toggle favorite"
                    )
                }
                is AppResult.Loading -> {}
            }
        }
    }

    fun isFavorite(channelId: String): kotlinx.coroutines.flow.Flow<Boolean> =
        favoritesRepository.isFavorite(channelId)

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
