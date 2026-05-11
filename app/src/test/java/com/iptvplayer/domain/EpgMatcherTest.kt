package com.iptvplayer.domain

import com.iptvplayer.domain.model.Channel
import com.iptvplayer.domain.model.EpgChannel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class EpgMatcherTest {

    @Test
    fun `match EPG channels to playlist channels by tvg-id`() {
        val playlistChannels = listOf(
            Channel(id = "1", playlistId = "p1", tvgId = "BBC1.uk", name = "BBC One", number = "1", streamUrl = "http://..."),
            Channel(id = "2", playlistId = "p1", tvgId = "ITV1.uk", name = "ITV", number = "3", streamUrl = "http://..."),
        )
        val epgChannels = listOf(
            EpgChannel(id = "BBC1.uk", displayName = "BBC One", iconUrl = null, programmes = emptyList()),
            EpgChannel(id = "ITV1.uk", displayName = "ITV", iconUrl = null, programmes = emptyList()),
        )

        val matched = EpgMatcher.match(playlistChannels, epgChannels)

        assertEquals("BBC One", matched[0].epgChannel?.displayName)
        assertEquals("ITV", matched[1].epgChannel?.displayName)
    }

    @Test
    fun `unmatched channels have null epgChannel`() {
        val playlistChannels = listOf(
            Channel(id = "1", playlistId = "p1", tvgId = "UNKNOWN", name = "Unknown", number = "1", streamUrl = "http://..."),
        )
        val epgChannels = listOf(
            EpgChannel(id = "BBC1.uk", displayName = "BBC One", iconUrl = null, programmes = emptyList()),
        )

        val matched = EpgMatcher.match(playlistChannels, epgChannels)

        assertEquals(1, matched.size)
        assertNull(matched[0].epgChannel)
    }

    @Test
    fun `match by name fallback when tvg-id is null`() {
        val playlistChannels = listOf(
            Channel(id = "1", playlistId = "p1", tvgId = null, name = "BBC One", number = "1", streamUrl = "http://..."),
        )
        val epgChannels = listOf(
            EpgChannel(id = "BBC1.uk", displayName = "BBC One", iconUrl = null, programmes = emptyList()),
        )

        val matched = EpgMatcher.match(playlistChannels, epgChannels)

        assertEquals("BBC One", matched[0].epgChannel?.displayName)
    }

    @Test
    fun `match is case-insensitive`() {
        val playlistChannels = listOf(
            Channel(id = "1", playlistId = "p1", tvgId = "bbc1.uk", name = "BBC One", number = "1", streamUrl = "http://..."),
        )
        val epgChannels = listOf(
            EpgChannel(id = "BBC1.UK", displayName = "BBC One", iconUrl = null, programmes = emptyList()),
        )

        val matched = EpgMatcher.match(playlistChannels, epgChannels)

        assertNotNull(matched[0].epgChannel)
    }
}
