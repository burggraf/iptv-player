package com.iptvplayer.domain

import com.iptvplayer.domain.model.Channel
import com.iptvplayer.domain.model.EpgChannel

/**
 * Matches EPG channels to playlist channels.
 *
 * Strategy:
 * 1. Match by tvg-id (exact, case-insensitive)
 * 2. Fallback: match by display name vs channel name (exact, case-insensitive)
 */
object EpgMatcher {

    fun match(
        playlistChannels: List<Channel>,
        epgChannels: List<EpgChannel>
    ): List<Channel> {
        val epgById = epgChannels.associateBy { it.id.lowercase() }
        val epgByName = epgChannels.associateBy { it.displayName.lowercase() }

        return playlistChannels.map { channel ->
            val epgChannel = channel.tvgId?.let { tvgId ->
                epgById[tvgId.lowercase()]
            } ?: epgByName[channel.name.lowercase()]

            channel.copy(epgChannel = epgChannel)
        }
    }
}
