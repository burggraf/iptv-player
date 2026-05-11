package com.iptvplayer.domain.model

sealed class PlaybackState {
    object Idle : PlaybackState()
    object Loading : PlaybackState()
    object Buffering : PlaybackState()
    data class Playing(val position: Long, val duration: Long) : PlaybackState()
    data class Error(val message: String, val recoverable: Boolean) : PlaybackState()
}
