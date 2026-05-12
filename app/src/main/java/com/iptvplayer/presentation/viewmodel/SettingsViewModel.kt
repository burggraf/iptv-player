package com.iptvplayer.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptvplayer.domain.model.AppSettings
import com.iptvplayer.domain.model.BufferSize
import com.iptvplayer.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class SettingsUiState(
    val settings: AppSettings = AppSettings(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val successMessage: String? = null,
)

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        settingsRepository.getSettings()
            .onEach { settings ->
                _uiState.value = _uiState.value.copy(settings = settings, isLoading = false)
            }
            .launchIn(viewModelScope)
    }

    fun updateEpgRefreshInterval(hours: Int) {
        saveSettings { copy(epgRefreshIntervalHours = hours) }
    }

    fun updatePixelPerMinute(value: Int) {
        saveSettings { copy(pixelPerMinute = value) }
    }

    fun updateChannelSwitchDelay(ms: Long) {
        saveSettings { copy(channelSwitchDelayMs = ms) }
    }

    fun toggleStartOnLastChannel() {
        saveSettings { copy(startOnLastChannel = !startOnLastChannel) }
    }

    fun toggleShowChannelNumbers() {
        saveSettings { copy(showChannelNumbers = !showChannelNumbers) }
    }

    fun updateBufferSize(size: BufferSize) {
        saveSettings { copy(bufferSize = size) }
    }

    private fun saveSettings(update: AppSettings.() -> AppSettings) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                settingsRepository.updateSettings(update)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Settings saved",
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to save settings",
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}
