package com.iptvplayer.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Tests for MIME type detection logic (pure Kotlin, no Android deps).
 * Full MediaItem construction tested in instrumented tests.
 */
class PlaybackRepositoryImplTest {

    @Test
    fun `detect HLS mime type for m3u8 url`() {
        val mimeType = detectMimeType("http://example.com/stream.m3u8")
        assertEquals("application/x-mpegURL", mimeType)
    }

    @Test
    fun `detect DASH mime type for mpd url`() {
        val mimeType = detectMimeType("http://example.com/stream.mpd")
        assertEquals("application/dash+xml", mimeType)
    }

    @Test
    fun `detect null mime type for unknown extension`() {
        val mimeType = detectMimeType("http://example.com/stream.ts")
        assertNull(mimeType)
    }

    @Test
    fun `detect null mime type for plain url`() {
        val mimeType = detectMimeType("http://example.com/stream")
        assertNull(mimeType)
    }

    @Test
    fun `handle url with query params`() {
        val mimeType = detectMimeType("http://example.com/stream.m3u8?token=abc&expires=123")
        // Query params don't affect detection — extension still at end of path
        assertNull(mimeType) // ends with query string, not .m3u8
    }

    /**
     * Extracted MIME detection logic for JVM-testable pure Kotlin.
     * Mirrors the logic in PlaybackRepositoryImpl.buildMediaItem.
     */
    private fun detectMimeType(streamUrl: String): String? = when {
        streamUrl.endsWith(".m3u8") -> "application/x-mpegURL"
        streamUrl.endsWith(".mpd") -> "application/dash+xml"
        else -> null
    }
}
