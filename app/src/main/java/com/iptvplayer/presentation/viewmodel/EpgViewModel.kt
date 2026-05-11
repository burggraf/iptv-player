package com.iptvplayer.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptvplayer.core.AppResult
import com.iptvplayer.domain.model.Channel
import com.iptvplayer.domain.model.EpgProgramme
import com.iptvplayer.domain.usecase.FetchEpgUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EpgUiState(
    val channels: List<Channel> = emptyList(),
    val programmes: Map<String, List<EpgProgramme>> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
)

class EpgViewModel(
    private val fetchEpgUseCase: FetchEpgUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(EpgUiState())
    val uiState: StateFlow<EpgUiState> = _uiState.asStateFlow()

    fun fetchEpg(playlistIds: List<String>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = fetchEpgUseCase(playlistIds)) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
                is AppResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.exception.message
                    )
                }
                is AppResult.Loading -> {}
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    val filteredChannels: StateFlow<List<Channel>> =
        MutableStateFlow(emptyList()) // TODO: derive from uiState + searchQuery
}
