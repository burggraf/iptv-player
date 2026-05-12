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
import java.time.Instant

data class EpgUiState(
    val channels: List<Channel> = emptyList(),
    val programmes: Map<String, List<EpgProgramme>> = emptyMap(),
    val groups: List<String> = emptyList(),
    val selectedGroup: String = GROUP_ALL,
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
) {
    val filteredChannels: List<Channel>
        get() {
            var result = channels
            if (selectedGroup != GROUP_ALL) {
                result = result.filter { it.group == selectedGroup }
            }
            if (searchQuery.isNotBlank()) {
                val q = searchQuery.lowercase()
                result = result.filter { ch ->
                    ch.name.lowercase().contains(q) ||
                        ch.number.contains(q) ||
                        (ch.group?.lowercase()?.contains(q) == true)
                }
            }
            return result
        }

    fun getNowPlaying(channelId: String): EpgProgramme? {
        val now = Instant.now()
        return programmes[channelId]?.firstOrNull { p ->
            p.startAt <= now && p.endAt > now
        }
    }

    fun getUpcoming(channelId: String, count: Int = 3): List<EpgProgramme> {
        val now = Instant.now()
        return programmes[channelId]
            ?.filter { it.startAt >= now }
            ?.take(count)
            ?: emptyList()
    }

    companion object {
        const val GROUP_ALL = "All"
    }
}

class EpgViewModel(
    private val fetchEpgUseCase: FetchEpgUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(EpgUiState())
    val uiState: StateFlow<EpgUiState> = _uiState.asStateFlow()

    fun getFilteredChannels(): List<Channel> = uiState.value.filteredChannels

    fun loadChannels(channels: List<Channel>) {
        val groups = listOf(EpgUiState.GROUP_ALL) + channels
            .mapNotNull { it.group }
            .distinct()
            .sorted()

        _uiState.value = _uiState.value.copy(
            channels = channels,
            groups = groups,
            isLoading = false,
            error = null
        )
    }

    fun setProgrammes(programmes: Map<String, List<EpgProgramme>>) {
        _uiState.value = _uiState.value.copy(
            programmes = programmes,
            isLoading = false,
            error = null
        )
    }

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

    fun selectGroup(group: String) {
        _uiState.value = _uiState.value.copy(selectedGroup = group)
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }
}

