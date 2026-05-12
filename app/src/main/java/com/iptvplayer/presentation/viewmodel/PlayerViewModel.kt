package com.iptvplayer.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import com.iptvplayer.core.AppResult
import com.iptvplayer.domain.model.Channel
import com.iptvplayer.domain.model.PlaybackState
import com.iptvplayer.domain.repository.PlaybackRepository
import com.iptvplayer.domain.usecase.PlayChannelUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val playbackRepository: PlaybackRepository,
    private val playChannelUseCase: PlayChannelUseCase,
) : ViewModel() {

    val player: Player
        get() = playbackRepository.getPlayer()

    private val _currentChannel = MutableStateFlow<Channel?>(null)
    val currentChannel: StateFlow<Channel?> = _currentChannel.asStateFlow()

    private val _playbackState = MutableStateFlow<PlaybackState>(PlaybackState.Idle)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    init {
        viewModelScope.launch {
            playbackRepository.getPlaybackState().collect { state ->
                _playbackState.value = state
            }
        }
    }

    private val _isFullscreen = MutableStateFlow(false)
    val isFullscreen: StateFlow<Boolean> = _isFullscreen.asStateFlow()

    fun selectChannel(channel: Channel) {
        _currentChannel.value = channel
        viewModelScope.launch {
            when (val result = playChannelUseCase(channel)) {
                is AppResult.Success -> {
                    // State managed by repository via Player.Listener
                }
                is AppResult.Error -> {
                    // Error already set by repository
                }
                is AppResult.Loading -> {}
            }
        }
    }

    fun setFullscreen(fullscreen: Boolean) {
        _isFullscreen.value = fullscreen
    }

    fun toggleFullscreen() {
        _isFullscreen.value = !_isFullscreen.value
    }

    fun switchChannel(direction: Int) {
        // Switch to previous/next channel — placeholder for channel list access
        // In production, inject channel list and index tracking
    }

    override fun onCleared() {
        super.onCleared()
        playbackRepository.releasePlayer()
    }
}
