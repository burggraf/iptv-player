package com.iptvplayer.data.parser

import com.iptvplayer.domain.model.Channel
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class XtreamParserTest {

    private val parser = XtreamParser()

    @Test
    fun `parse auth success response`() {
        val json = """
            {
                "user_info": {
                    "auth": true,
                    "username": "demo",
                    "status": "Active",
                    "exp_date": "1893456000",
                    "is_trial": "0",
                    "max_connections": "1"
                },
                "server_info": {
                    "url": "http://example.com",
                    "port": "80",
                    "server_protocol": "http"
                }
            }
        """.trimIndent()

        val result = parser.parseAuth(json)

        assertTrue(result.success)
        assertEquals("demo", result.userInfo?.username)
        assertEquals("Active", result.userInfo?.status)
        assertEquals("http://example.com", result.serverInfo?.url)
    }

    @Test
    fun `parse auth failure response`() {
        val json = """
            {
                "user_info": {
                    "auth": false,
                    "message": "Invalid credentials"
                }
            }
        """.trimIndent()

        val result = parser.parseAuth(json)

        assertFalse(result.success)
    }

    @Test
    fun `parse live categories`() {
        val json = """
            [
                {"category_id": "1", "category_name": "Entertainment"},
                {"category_id": "2", "category_name": "Sports"},
                {"category_id": "3", "category_name": "News"}
            ]
        """.trimIndent()

        val categories = parser.parseCategories(json)

        assertEquals(3, categories.size)
        assertEquals("Entertainment", categories[0].categoryName)
        assertEquals("Sports", categories[1].categoryName)
    }

    @Test
    fun `parse live streams to channels`() {
        val json = """
            [
                {
                    "num": 1,
                    "name": "BBC One HD",
                    "stream_type": "live",
                    "stream_id": 101,
                    "stream_icon": "http://example.com/bbc1.png",
                    "tv_archive": 0,
                    "epg_channel_id": "BBC1.uk",
                    "category_id": "1"
                },
                {
                    "num": 2,
                    "name": "Sky Sports",
                    "stream_type": "live",
                    "stream_id": 102,
                    "tv_archive": 3,
                    "category_id": "2"
                }
            ]
        """.trimIndent()

        val channels = parser.parseStreams(json, playlistId = "p1", serverUrl = "http://example.com")

        assertEquals(2, channels.size)
        with(channels[0]) {
            assertEquals("BBC One HD", name)
            assertEquals("1", number)
            assertEquals("http://example.com/bbc1.png", logo)
            assertEquals("BBC1.uk", tvgId)
            assertEquals("http://example.com/live/101.m3u8", streamUrl)
            assertEquals(0, catchupDays)
        }
        with(channels[1]) {
            assertEquals("Sky Sports", name)
            assertEquals(3, catchupDays)
        }
    }

    @Test
    fun `parse empty stream list`() {
        val channels = parser.parseStreams("[]", playlistId = "p1", serverUrl = "http://example.com")
        assertTrue(channels.isEmpty())
    }
}
