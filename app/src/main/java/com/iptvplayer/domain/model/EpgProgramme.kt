package com.iptvplayer.domain.model

import java.time.Instant

data class EpgProgramme(
    val id: String,
    val channelId: String,
    val title: String,
    val description: String? = null,
    val category: String? = null,
    val startAt: Instant,
    val endAt: Instant,
    val iconUrl: String? = null,
    val season: String? = null,
    val episode: String? = null,
    val new: Boolean = false,
    val premiere: Boolean = false,
)
