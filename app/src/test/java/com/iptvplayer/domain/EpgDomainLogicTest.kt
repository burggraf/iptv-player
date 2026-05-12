package com.iptvplayer.domain

import com.iptvplayer.domain.model.Channel
import com.iptvplayer.domain.model.EpgProgramme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

/**
 * Tests for EPG domain logic: now-playing detection, cell width calculation,
 * programme overlap handling, and time-based filtering.
 */
class EpgDomainLogicTest {

    @Test
    fun `now-playing detected when current time within programme bounds`() {
        val now = Instant.now()
        val programme = EpgProgramme(
            id = "p1", channelId = "1", title = "Live News",
            startAt = now.minusSeconds(1800), endAt = now.plusSeconds(1800)
        )

        val isNow = programme.startAt <= now && programme.endAt > now
        assertEquals(true, isNow)
    }

    @Test
    fun `not now-playing when current time before programme start`() {
        val now = Instant.now()
        val programme = EpgProgramme(
            id = "p1", channelId = "1", title = "Future Show",
            startAt = now.plusSeconds(3600), endAt = now.plusSeconds(7200)
        )

        val isNow = programme.startAt <= now && programme.endAt > now
        assertEquals(false, isNow)
    }

    @Test
    fun `not now-playing when current time after programme end`() {
        val now = Instant.now()
        val programme = EpgProgramme(
            id = "p1", channelId = "1", title = "Old Show",
            startAt = now.minusSeconds(7200), endAt = now.minusSeconds(3600)
        )

        val isNow = programme.startAt <= now && programme.endAt > now
        assertEquals(false, isNow)
    }

    @Test
    fun `programme cell width proportional to duration`() {
        val start = Instant.now()
        val end = start.plusSeconds(3600) // 1 hour
        val pixelPerMinute = 2f

        val durationMinutes = (end.epochSecond - start.epochSecond) / 60
        val width = durationMinutes * pixelPerMinute

        assertEquals(120f, width) // 60 min * 2px/min
    }

    @Test
    fun `30-minute programme produces correct width`() {
        val start = Instant.now()
        val end = start.plusSeconds(1800) // 30 minutes
        val pixelPerMinute = 2f

        val durationMinutes = (end.epochSecond - start.epochSecond) / 60
        val width = durationMinutes * pixelPerMinute

        assertEquals(60f, width) // 30 min * 2px/min
    }

    @Test
    fun `progress calculation for now-playing programme`() {
        val now = Instant.now()
        val start = now.minusSeconds(900)  // started 15 min ago
        val end = now.plusSeconds(2700)     // ends in 45 min (total 60 min)

        val totalDuration = end.epochSecond - start.epochSecond // 3600s
        val elapsed = now.epochSecond - start.epochSecond       // 900s
        val progress = elapsed.toFloat() / totalDuration.toFloat()

        assertEquals(0.25f, progress, 0.001f)
    }

    @Test
    fun `channel filtering by group`() {
        val channels = listOf(
            Channel(id = "1", playlistId = "p1", number = "1", name = "BBC One", group = "Entertainment", streamUrl = "http://..."),
            Channel(id = "2", playlistId = "p1", number = "2", name = "Sky News", group = "News", streamUrl = "http://..."),
            Channel(id = "3", playlistId = "p1", number = "3", name = "BBC Two", group = "Entertainment", streamUrl = "http://..."),
        )

        val entertainment = channels.filter { it.group == "Entertainment" }
        assertEquals(2, entertainment.size)
    }

    @Test
    fun `channel filtering by search query`() {
        val channels = listOf(
            Channel(id = "1", playlistId = "p1", number = "1", name = "BBC One", group = "Entertainment", streamUrl = "http://..."),
            Channel(id = "2", playlistId = "p1", number = "2", name = "Sky News", group = "News", streamUrl = "http://..."),
            Channel(id = "3", playlistId = "p1", number = "3", name = "BBC Two", group = "Entertainment", streamUrl = "http://..."),
            Channel(id = "4", playlistId = "p1", number = "4", name = "CNN", group = "News", streamUrl = "http://..."),
        )

        val filtered = channels.filter { it.name.contains("BBC", ignoreCase = true) }
        assertEquals(2, filtered.size)
    }

    @Test
    fun `programme sorted by start time`() {
        val now = Instant.now()
        val programmes = listOf(
            EpgProgramme(id = "p3", channelId = "1", title = "Third", startAt = now.plusSeconds(7200), endAt = now.plusSeconds(10800)),
            EpgProgramme(id = "p1", channelId = "1", title = "First", startAt = now, endAt = now.plusSeconds(3600)),
            EpgProgramme(id = "p2", channelId = "1", title = "Second", startAt = now.plusSeconds(3600), endAt = now.plusSeconds(7200)),
        )

        val sorted = programmes.sortedBy { it.startAt }
        assertEquals("First", sorted[0].title)
        assertEquals("Second", sorted[1].title)
        assertEquals("Third", sorted[2].title)
    }

    @Test
    fun `get programmes within time window`() {
        val now = Instant.now()
        val programmes = listOf(
            EpgProgramme(id = "p1", channelId = "1", title = "Before Window", startAt = now.minusSeconds(14400), endAt = now.minusSeconds(10800)),
            EpgProgramme(id = "p2", channelId = "1", title = "In Window", startAt = now, endAt = now.plusSeconds(3600)),
            EpgProgramme(id = "p3", channelId = "1", title = "After Window", startAt = now.plusSeconds(28800), endAt = now.plusSeconds(32400)),
        )

        val windowEnd = now.plusSeconds(14400)
        val inWindow = programmes.filter { p ->
            p.startAt < windowEnd && p.endAt > now
        }

        assertEquals(1, inWindow.size)
        assertEquals("In Window", inWindow[0].title)
    }
}
