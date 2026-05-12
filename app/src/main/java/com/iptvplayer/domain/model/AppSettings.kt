package com.iptvplayer.domain.model

enum class BufferSize(val label: String) {
    SMALL("Low"),
    MEDIUM("Medium"),
    LARGE("High"),
}

data class AppSettings(
    val epgRefreshIntervalHours: Int = 6,
    val pixelPerMinute: Int = 2,
    val channelSwitchDelayMs: Long = 300,
    val startOnLastChannel: Boolean = true,
    val showChannelNumbers: Boolean = true,
    val bufferSize: BufferSize = BufferSize.MEDIUM,
)
