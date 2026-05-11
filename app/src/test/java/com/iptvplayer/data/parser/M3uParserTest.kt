package com.iptvplayer.data.parser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class M3uParserTest {

    private val parser = M3uParser()

    @Test
    fun `parse basic M3U line extracts channel info`() {
        val input = """
            #EXTM3U
            #EXTINF:-1 tvg-id="BBC1.uk" tvg-chno="1" tvg-name="BBC One" tvg-logo="https://example.com/bbc1.png" group-title="UK",BBC One HD
            http://stream.example.com/bbc1.m3u8
        """.trimIndent()

        val channels = parser.parse(input)

        assertEquals(1, channels.size)
        with(channels[0]) {
            assertEquals("BBC One HD", name)
            assertEquals("1", number)
            assertEquals("BBC1.uk", tvgId)
            assertEquals("UK", group)
            assertEquals("https://example.com/bbc1.png", logo)
            assertEquals("http://stream.example.com/bbc1.m3u8", streamUrl)
        }
    }

    @Test
    fun `parse M3U groups channels by group-title`() {
        val input = """
            #EXTM3U
            #EXTINF:-1 group-title="Sports",Sky Sports 1
            http://example.com/sky1
            #EXTINF:-1 group-title="Sports",Sky Sports 2
            http://example.com/sky2
            #EXTINF:-1 group-title="News",BBC News
            http://example.com/bbcnews
        """.trimIndent()

        val channels = parser.parse(input)
        val groups = channels.groupBy { it.group }

        assertEquals(2, groups["Sports"]?.size)
        assertEquals(1, groups["News"]?.size)
    }

    @Test
    fun `parse catchup attributes`() {
        val input = """
            #EXTM3U
            #EXTINF:-1 catchup="default" catchup-days="3" catchup-source="https://example.com/${'$'}{utc}",Channel 4
            http://example.com/ch4
        """.trimIndent()

        val channels = parser.parse(input)

        assertEquals(1, channels.size)
        assertEquals(3, channels[0].catchupDays)
        assertEquals("https://example.com/${'$'}{utc}", channels[0].catchupSource)
    }

    @Test
    fun `parse ignores empty lines and comments`() {
        val input = """
            #EXTM3U

            # Some comment
            #EXTINF:-1,BBC One
            http://example.com/bbc1

            # Another comment
            #EXTINF:-1,ITV
            http://example.com/itv
        """.trimIndent()

        val channels = parser.parse(input)

        assertEquals(2, channels.size)
    }

    @Test
    fun `parse handles missing optional attributes`() {
        val input = """
            #EXTM3U
            #EXTINF:-1,Simple Channel
            http://example.com/simple
        """.trimIndent()

        val channels = parser.parse(input)

        assertEquals(1, channels.size)
        assertEquals("Simple Channel", channels[0].name)
        assertNull(channels[0].tvgId)
        assertNull(channels[0].group)
        assertNull(channels[0].logo)
    }

    @Test
    fun `parse returns empty list for EXTM3U only`() {
        val input = "#EXTM3U"
        val channels = parser.parse(input)
        assertTrue(channels.isEmpty())
    }

    @Test
    fun `parse returns empty list for empty input`() {
        val channels = parser.parse("")
        assertTrue(channels.isEmpty())
    }

    @Test
    fun `parse multiple channels`() {
        val input = """
            #EXTM3U
            #EXTINF:-1 tvg-id="BBC1.uk" tvg-chno="1" group-title="Entertainment",BBC One
            http://example.com/bbc1
            #EXTINF:-1 tvg-id="BBC2.uk" tvg-chno="2" group-title="Entertainment",BBC Two
            http://example.com/bbc2
            #EXTINF:-1 tvg-id="ITV1.uk" tvg-chno="3" group-title="Entertainment",ITV
            http://example.com/itv
        """.trimIndent()

        val channels = parser.parse(input)

        assertEquals(3, channels.size)
        assertEquals("BBC One", channels[0].name)
        assertEquals("BBC Two", channels[1].name)
        assertEquals("ITV", channels[2].name)
    }
}
