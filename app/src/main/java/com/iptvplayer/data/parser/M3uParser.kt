package com.iptvplayer.data.parser

import com.iptvplayer.domain.model.Channel

/**
 * M3U playlist parser.
 *
 * State machine: parse line-by-line, track #EXTINF attributes,
 * consume next line as stream URL.
 *
 * Regex for attribute extraction: (\w+(?:-\w+)*)="([^"]*)" for key-value pairs.
 */
class M3uParser {

    private val attributeRegex = Regex("""(\w+(?:-\w+)*)="([^"]*)"""")
    private val displayNameRegex = Regex(""",(.+)$""")

    fun parse(input: String): List<Channel> {
        val lines = input.lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toList()

        val channels = mutableListOf<Channel>()
        var i = 0

        while (i < lines.size) {
            val line = lines[i]

            if (line.startsWith("#EXTINF")) {
                val attributes = parseAttributes(line)
                val displayName = parseDisplayName(line)
                val streamUrl = if (i + 1 < lines.size) lines[i + 1] else ""

                // Skip URL lines that look like headers
                if (streamUrl.startsWith("#")) {
                    i++
                    continue
                }

                if (streamUrl.isNotEmpty()) {
                    channels.add(
                        Channel(
                            id = attributes["tvg-id"] ?: displayName,
                            playlistId = "", // set by caller
                            number = attributes["tvg-chno"] ?: "",
                            name = displayName,
                            logo = attributes["tvg-logo"],
                            group = attributes["group-title"],
                            tvgId = attributes["tvg-id"],
                            streamUrl = streamUrl,
                            catchupDays = attributes["catchup-days"]?.toIntOrNull() ?: 0,
                            catchupSource = attributes["catchup-source"],
                        )
                    )
                    i += 2
                } else {
                    i++
                }
            } else {
                i++
            }
        }

        return channels
    }

    private fun parseAttributes(line: String): Map<String, String> {
        return attributeRegex.findAll(line)
            .associate { it.groupValues[1] to it.groupValues[2] }
    }

    private fun parseDisplayName(line: String): String {
        return displayNameRegex.find(line)?.groupValues?.get(1)?.trim() ?: ""
    }
}
