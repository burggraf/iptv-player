package com.iptvplayer.data.parser

import com.iptvplayer.domain.model.EpgChannel

/**
 * XMLTV EPG parser.
 * Phase 1 — will implement XmlPullParser streaming parse for large files.
 * Memory-efficient: don't load entire EPG into memory; chunk by date.
 */
class XmlTvParser {
    // TODO: XmlPullParser streaming parse
    // Date format: yyyyMMddHHmmss Z → Instant
    // Chunk by date for memory efficiency
    fun parse(xml: String): List<EpgChannel> = emptyList()
}
