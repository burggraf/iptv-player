package com.iptvplayer.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptvplayer.core.AppResult
import com.iptvplayer.domain.model.Playlist
import com.iptvplayer.domain.model.PlaylistType
import com.iptvplayer.domain.usecase.LoadPlaylistUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PlaylistUiState(
    val playlists: List<Playlist> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

class PlaylistViewModel(
    private val loadPlaylistUseCase: LoadPlaylistUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlaylistUiState())
    val uiState: StateFlow<PlaylistUiState> = _uiState.asStateFlow()

    fun addPlaylist(
        name: String,
        type: PlaylistType,
        url: String,
        username: String? = null,
        password: String? = null,
        serverUrl: String? = null,
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val playlist = Playlist(
                id = java.util.UUID.randomUUID().toString(),
                name = name,
                type = type,
                url = url,
                username = username,
                password = password,
                serverUrl = serverUrl,
            )

            when (val result = loadPlaylistUseCase(playlist)) {
                is AppResult.Success -> {
                    // TODO: refresh playlists list from repository
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
}
