package com.iptvplayer.data.parser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

/**
 * Phase 7 — M3U Parser Edge Case & Stress Tests.
 * Tests malformed input, large files, and boundary conditions.
 */
class M3uParserEdgeCaseTest {

    private val parser = M3uParser()

    @Test
    fun `parse empty input returns empty list`() {
        val channels = parser.parse("")
        assertEquals(0, channels.size)
    }

    @Test
    fun `parse only header returns empty list`() {
        val channels = parser.parse("#EXTM3U")
        assertEquals(0, channels.size)
    }

    @Test
    fun `parse empty lines and comments ignored`() {
        val input = """
            #EXTM3U
            # This is a comment

            #EXTINF:-1,Channel One
            http://example.com/one

            # Another comment

            #EXTINF:-1,Channel Two
            http://example.com/two
        """.trimIndent()

        val channels = parser.parse(input)
        assertEquals(2, channels.size)
        assertEquals("Channel One", channels[0].name)
        assertEquals("Channel Two", channels[1].name)
    }

    @Test
    fun `parse channel name with commas`() {
        val input = """
            #EXTINF:-1,BBC One, HD (UK)
            http://example.com/bbc
        """.trimIndent()

        val channels = parser.parse(input)
        assertEquals(1, channels.size)
        assertEquals("BBC One, HD (UK)", channels[0].name)
    }

    @Test
    fun `parse url with query parameters`() {
        val input = """
            #EXTINF:-1,Test Channel
            http://example.com/stream?token=abc&user=123&type=m3u8
        """.trimIndent()

        val channels = parser.parse(input)
        assertEquals(1, channels.size)
        assertEquals("http://example.com/stream?token=abc&user=123&type=m3u8", channels[0].streamUrl)
    }

    @Test
    fun `parse special characters in attributes`() {
        val input = """
            #EXTINF:-1 tvg-id="TV&News" tvg-name="TV & News" tvg-logo="https://example.com/logo.png?size=large",TV & News
            http://example.com/tvnews
        """.trimIndent()

        val channels = parser.parse(input)
        assertEquals(1, channels.size)
        assertEquals("TV&News", channels[0].tvgId)
        assertEquals("TV & News", channels[0].name)
    }

    @Test
    fun `parse large playlist performs efficiently`() {
        val sb = StringBuilder("#EXTM3U\n")
        for (i in 1..1000) {
            sb.appendLine("#EXTINF:-1 tvg-chno=\"$i\" tvg-name=\"Channel $i\" group-title=\"Group ${i % 10}\",Channel $i")
            sb.appendLine("http://example.com/stream$i")
        }

        val start = System.currentTimeMillis()
        val channels = parser.parse(sb.toString())
        val elapsed = System.currentTimeMillis() - start

        assertEquals(1000, channels.size)
        assertTrue("Parsing 1000 channels took ${elapsed}ms, should be < 500ms", elapsed < 500)
        assertEquals(10, channels.groupBy { it.group }.size)
    }

    @Test
    fun `parse malformed extinf skips invalid line`() {
        val input = """
            #EXTM3U
            #EXTINF:-1
            http://example.com/no-name
            #EXTINF:bad-duration,Valid After Bad
            http://example.com/valid
        """.trimIndent()

        val channels = parser.parse(input)
        assertTrue(channels.size >= 1)
    }

    @Test
    fun `parse unicode channel names`() {
        val input = """
            #EXTINF:-1,Çáñálé Ñéws
            http://example.com/unicode
        """.trimIndent()

        val channels = parser.parse(input)
        assertEquals(1, channels.size)
        assertEquals("Çáñálé Ñéws", channels[0].name)
    }
}
