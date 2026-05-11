package com.iptvplayer.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptvplayer.core.Result
import com.iptvplayer.domain.model.Channel
import com.iptvplayer.domain.model.PlaybackState
import com.iptvplayer.domain.usecase.PlayChannelUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val playChannelUseCase: PlayChannelUseCase
) : ViewModel() {

    private val _currentChannel = MutableStateFlow<Channel?>(null)
    val currentChannel: StateFlow<Channel?> = _currentChannel.asStateFlow()

    private val _playbackState = MutableStateFlow<PlaybackState>(PlaybackState.Idle)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _isFullscreen = MutableStateFlow(false)
    val isFullscreen: StateFlow<Boolean> = _isFullscreen.asStateFlow()

    fun selectChannel(channel: Channel) {
        _currentChannel.value = channel
        _playbackState.value = PlaybackState.Loading

        viewModelScope.launch {
            when (val result = playChannelUseCase(channel)) {
                is Result.Success -> {
                    _playbackState.value = PlaybackState.Playing(0L, 0L)
                }
                is Result.Error -> {
                    _playbackState.value = PlaybackState.Error(
                        message = result.exception.message ?: "Unknown error",
                        recoverable = true
                    )
                }
                is Result.Loading -> {}
            }
        }
    }

    fun setFullscreen(fullscreen: Boolean) {
        _isFullscreen.value = fullscreen
    }
}
