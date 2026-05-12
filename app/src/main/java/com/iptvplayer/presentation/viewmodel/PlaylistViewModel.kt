package com.iptvplayer.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptvplayer.core.AppResult
import com.iptvplayer.domain.model.Playlist
import com.iptvplayer.domain.model.PlaylistType
import com.iptvplayer.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.Instant

data class PlaylistUiState(
    val playlists: List<Playlist> = emptyList(),
    val selectedPlaylistId: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
)

class PlaylistViewModel(
    private val playlistRepository: PlaylistRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlaylistUiState())
    val uiState: StateFlow<PlaylistUiState> = _uiState.asStateFlow()

    init {
        loadPlaylists()
    }

    private fun loadPlaylists() {
        playlistRepository.getPlaylists()
            .onEach { playlists ->
                val currentSelected = _uiState.value.selectedPlaylistId
                    ?.takeIf { playlists.any { it.id == _uiState.value.selectedPlaylistId } }
                _uiState.value = _uiState.value.copy(
                    playlists = playlists,
                    selectedPlaylistId = currentSelected ?: playlists.firstOrNull()?.id,
                )
            }
            .launchIn(viewModelScope)
    }

    fun selectPlaylist(id: String) {
        _uiState.value = _uiState.value.copy(selectedPlaylistId = id)
    }

    fun addPlaylist(
        name: String,
        type: PlaylistType,
        url: String? = null,
        username: String? = null,
        password: String? = null,
        serverUrl: String? = null,
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, successMessage = null)

            val playlist = Playlist(
                id = java.util.UUID.randomUUID().toString(),
                name = name,
                type = type,
                url = url,
                username = username,
                password = password,
                serverUrl = serverUrl,
                addedAt = Instant.now(),
                lastUpdated = Instant.now(),
            )

            android.util.Log.d("PlaylistVM", "Adding playlist: $name, type=$type, serverUrl=$serverUrl")

            when (val result = playlistRepository.addPlaylist(playlist)) {
                is AppResult.Success -> {
                    android.util.Log.d("PlaylistVM", "Playlist added successfully: $name")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Playlist \"$name\" added",
                    )
                }
                is AppResult.Error -> {
                    android.util.Log.e("PlaylistVM", "Failed to add playlist: $name", result.exception)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.exception.message ?: "Failed to add playlist"
                    )
                }
                is AppResult.Loading -> {}
            }
        }
    }

    fun removePlaylist(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            when (val result = playlistRepository.removePlaylist(id)) {
                is AppResult.Success -> {
                    val newSelected = _uiState.value.playlists
                        .filter { it.id != id }
                        .firstOrNull()?.id
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        selectedPlaylistId = newSelected,
                        successMessage = "Playlist removed",
                    )
                }
                is AppResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.exception.message ?: "Failed to remove playlist"
                    )
                }
                is AppResult.Loading -> {}
            }
        }
    }

    fun refreshPlaylist(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            when (val result = playlistRepository.refreshPlaylist(id)) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Playlist refreshed",
                    )
                }
                is AppResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.exception.message ?: "Failed to refresh playlist"
                    )
                }
                is AppResult.Loading -> {}
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }

    fun getChannels(playlistId: String): List<com.iptvplayer.domain.model.Channel> {
        return playlistRepository.getChannels(playlistId)
            .let { kotlinx.coroutines.runBlocking { it.first() } }
    }
}
