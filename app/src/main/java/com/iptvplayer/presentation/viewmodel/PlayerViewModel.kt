package com.iptvplayer.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import com.iptvplayer.core.AppResult
import com.iptvplayer.domain.model.Channel
import com.iptvplayer.domain.model.PlaybackState
import com.iptvplayer.domain.repository.PlaybackRepository
import com.iptvplayer.domain.usecase.PlayChannelUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

    private val _channels = MutableStateFlow<List<Channel>>(emptyList())
    val channels: StateFlow<List<Channel>> = _channels.asStateFlow()

    private val _currentChannel = MutableStateFlow<Channel?>(null)
    val currentChannel: StateFlow<Channel?> = _currentChannel.asStateFlow()

    private val _playbackState = MutableStateFlow<PlaybackState>(PlaybackState.Idle)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    // Number pad input buffer
    private val _numberBuffer = MutableStateFlow("")
    val numberBuffer: StateFlow<String> = _numberBuffer.asStateFlow()

    private var numberInputJob: Job? = null

    init {
        viewModelScope.launch {
            playbackRepository.getPlaybackState().collect { state ->
                _playbackState.value = state
            }
        }
    }

    fun setChannels(channels: List<Channel>) {
        _channels.value = channels
    }

    fun selectChannel(channel: Channel) {
        _currentChannel.value = channel
        viewModelScope.launch {
            when (val result = playChannelUseCase(channel)) {
                is AppResult.Success -> {}
                is AppResult.Error -> {}
                is AppResult.Loading -> {}
            }
        }
    }

    /**
     * Play channel by ID.
     */
    fun playChannelById(channelId: String) {
        val channel = _channels.value.find { it.id == channelId } ?: return
        selectChannel(channel)
    }

    /**
     * Handle number pad input for quick channel switching.
     * Accumulates digits, resolves to channel after 1.5s timeout.
     */
    fun onNumberInput(digit: String) {
        if (digit !in "0123456789") return

        // Cancel pending resolution
        numberInputJob?.cancel()

        // Append digit (max 5 digits)
        val newBuffer = if (_numberBuffer.value.length < 5) {
            _numberBuffer.value + digit
        } else {
            digit
        }
        _numberBuffer.value = newBuffer

        // Try immediate match for exact channel number
        val matchingChannels = _channels.value.filter { it.number == newBuffer }
        if (matchingChannels.size == 1) {
            selectChannel(matchingChannels[0])
            _numberBuffer.value = ""
            return
        }

        // Auto-resolve after timeout
        numberInputJob = viewModelScope.launch {
            delay(1500)
            resolveAndPlayChannel(newBuffer)
            _numberBuffer.value = ""
        }
    }

    private fun resolveAndPlayChannel(numberStr: String) {
        // Try exact match first, then prefix match
        val exact = _channels.value.find { it.number == numberStr }
        if (exact != null) {
            selectChannel(exact)
            return
        }

        // Prefix match (user typed "1" and channel "10" exists)
        val prefix = _channels.value.filter { it.number.startsWith(numberStr) }
        if (prefix.size == 1) {
            selectChannel(prefix[0])
        }
    }

    /**
     * Switch to previous/next channel in the current list.
     * @param direction -1 for previous, +1 for next
     */
    fun switchChannel(direction: Int) {
        val current = _currentChannel.value ?: return
        val allChannels = _channels.value
        if (allChannels.isEmpty()) return

        val currentIndex = allChannels.indexOfFirst { it.id == current.id }
        if (currentIndex == -1) return

        val newIndex = (currentIndex + direction).mod(allChannels.size)
        selectChannel(allChannels[newIndex])
    }

    /**
     * Switch to previous/next channel within the same group.
     */
    fun switchChannelInGroup(direction: Int) {
        val current = _currentChannel.value ?: return
        val group = current.group
        val groupChannels = if (group != null) {
            _channels.value.filter { it.group == group }
        } else {
            _channels.value
        }
        if (groupChannels.isEmpty()) return

        val currentIndex = groupChannels.indexOfFirst { it.id == current.id }
        if (currentIndex == -1) return

        val newIndex = (currentIndex + direction).mod(groupChannels.size)
        selectChannel(groupChannels[newIndex])
    }

    fun setFullscreen(fullscreen: Boolean) {
        _isFullscreen.value = fullscreen
    }

    fun toggleFullscreen() {
        _isFullscreen.value = !_isFullscreen.value
    }

    private val _isFullscreen = MutableStateFlow(false)
    val isFullscreen: StateFlow<Boolean> = _isFullscreen.asStateFlow()

    override fun onCleared() {
        super.onCleared()
        numberInputJob?.cancel()
        playbackRepository.releasePlayer()
    }
}
