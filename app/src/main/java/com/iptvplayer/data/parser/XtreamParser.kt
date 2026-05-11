package com.iptvplayer.data.parser

import com.iptvplayer.domain.model.Channel

/**
 * Xtream Codes API parser.
 * Phase 1 — will implement JSON parsing for Xtream API responses.
 */
class XtreamParser {
    // TODO: Parse get_live_categories, get_live_streams responses
    // Map JSON to Channel list
    fun parseChannels(json: String): List<Channel> = emptyList()
}
