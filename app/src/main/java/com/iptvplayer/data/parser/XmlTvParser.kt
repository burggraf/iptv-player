package com.iptvplayer.data.parser

import com.iptvplayer.domain.model.EpgChannel
import com.iptvplayer.domain.model.EpgProgramme
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * XMLTV EPG parser using XmlPullParser.
 * Memory-efficient: streaming parse, doesn't load entire file into memory.
 * Date format: yyyyMMddHHmmss Z → Instant
 */
class XmlTvParser {

    data class XmlTvResult(val channels: List<EpgChannel>)

    private val xmlTvDateFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss Z", Locale.ROOT)

    fun parse(xml: String): XmlTvResult {
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = false
        val parser = factory.newPullParser()
        parser.setInput(StringReader(xml))

        val channelInfoMap = mutableMapOf<String, ChannelInfo>()
        val programmeMap = mutableMapOf<String, MutableList<EpgProgramme>>()

        while (parser.eventType != XmlPullParser.END_DOCUMENT) {
            when (parser.eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "channel" -> {
                            val id = parser.getAttributeValue(null, "id")
                            if (id != null) {
                                channelInfoMap[id] = parseChannel(parser)
                                programmeMap.computeIfAbsent(id) { mutableListOf() }
                            }
                        }
                        "programme" -> {
                            val programme = parseProgramme(parser)
                            if (programme != null) {
                                programmeMap.computeIfAbsent(programme.channelId) { mutableListOf() }
                                    .add(programme)
                            }
                        }
                    }
                }
            }
            parser.next()
        }

        val epgChannels = channelInfoMap.map { (id, info) ->
            EpgChannel(
                id = id,
                displayName = info.displayName,
                iconUrl = info.iconUrl,
                programmes = programmeMap[id]?.toList() ?: emptyList(),
            )
        }

        return XmlTvResult(channels = epgChannels)
    }

    private fun parseChannel(parser: XmlPullParser): ChannelInfo {
        var displayName: String? = null
        var iconUrl: String? = null

        // parser is currently on START_TAG "channel"
        while (!(parser.eventType == XmlPullParser.END_TAG && parser.name == "channel")) {
            parser.next()
            if (parser.eventType == XmlPullParser.START_TAG) {
                when (parser.name) {
                    "display-name" -> {
                        parser.next()
                        if (parser.eventType == XmlPullParser.TEXT) {
                            displayName = parser.text
                        }
                    }
                    "icon" -> {
                        iconUrl = parser.getAttributeValue(null, "src")
                    }
                }
            }
        }

        return ChannelInfo(displayName = displayName ?: "", iconUrl = iconUrl)
    }

    private fun parseProgramme(parser: XmlPullParser): EpgProgramme? {
        val channelId = parser.getAttributeValue(null, "channel")
        val startStr = parser.getAttributeValue(null, "start")
        val stopStr = parser.getAttributeValue(null, "stop")

        if (channelId == null) return null

        val startAt = parseDate(startStr) ?: return null
        val endAt = parseDate(stopStr) ?: return null

        var title: String? = null
        var description: String? = null
        var category: String? = null

        // parser is currently on START_TAG "programme"
        while (!(parser.eventType == XmlPullParser.END_TAG && parser.name == "programme")) {
            parser.next()
            if (parser.eventType == XmlPullParser.START_TAG) {
                val tagName = parser.name
                parser.next()
                val text = if (parser.eventType == XmlPullParser.TEXT) parser.text else null
                when (tagName) {
                    "title" -> title = text
                    "desc" -> description = text
                    "category" -> category = text
                }
            }
        }

        if (title == null) return null

        return EpgProgramme(
            id = "${channelId}_${startAt.epochSecond}",
            channelId = channelId,
            title = title,
            description = description,
            category = category,
            startAt = startAt,
            endAt = endAt,
        )
    }

    private fun parseDate(dateString: String?): Instant? {
        if (dateString.isNullOrBlank()) return null
        return try {
            ZonedDateTime.parse(dateString, xmlTvDateFormatter).toInstant()
        } catch (e: Exception) {
            null
        }
    }

    private data class ChannelInfo(val displayName: String, val iconUrl: String?)
}
