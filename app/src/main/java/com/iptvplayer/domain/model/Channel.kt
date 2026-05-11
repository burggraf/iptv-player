package com.iptvplayer.domain.model

data class Channel(
    val id: String,
    val playlistId: String,
    val number: String,
    val name: String,
    val logo: String? = null,
    val group: String? = null,
    val tvgId: String? = null,
    val streamUrl: String,
    val catchupDays: Int = 0,
    val catchupSource: String? = null,
    val epgChannel: EpgChannel? = null,
)
