package com.iptvplayer.domain.model

data class EpgChannel(
    val id: String,
    val displayName: String,
    val iconUrl: String?,
    val programmes: List<EpgProgramme> = emptyList(),
)
