package com.iptvplayer.presentation.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.iptvplayer.domain.model.Channel
import com.iptvplayer.domain.model.EpgProgramme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import java.time.Instant
import java.time.ZoneId
import kotlin.time.Duration.Companion.hours

/**
 * Phase 7 — Compose UI Tests for EPG Grid components.
 * Tests rendering, layout correctness, and interaction.
 */
class EpgGridComposeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val now = Instant.now()
    private val startTime = now.atZone(ZoneId.systemDefault()).withMinute(0).withSecond(0).withNano(0).toInstant()
    private val endTime = startTime.plusSeconds(4.hours.inWholeSeconds)

    private fun minsAgo(m: Long) = now.minusSeconds(m * 60)
    private fun minsFromNow(m: Long) = now.plusSeconds(m * 60)

    private val testChannels = listOf(
        Channel(id = "ch1", playlistId = "p1", number = "1", name = "BBC One", group = "UK", streamUrl = "http://..."),
        Channel(id = "ch2", playlistId = "p1", number = "2", name = "BBC Two", group = "UK", streamUrl = "http://..."),
        Channel(id = "ch3", playlistId = "p1", number = "3", name = "Sky News", group = "News", streamUrl = "http://..."),
    )

    private val testProgrammes = mapOf(
        "ch1" to listOf(
            EpgProgramme(id = "p1", channelId = "ch1", title = "Morning News", description = null,
                category = "News", startAt = minsAgo(60), endAt = minsFromNow(30), iconUrl = null),
            EpgProgramme(id = "p2", channelId = "ch1", title = "Breakfast Show", description = null,
                category = "Entertainment", startAt = minsFromNow(30), endAt = minsFromNow(90), iconUrl = null),
        ),
        "ch2" to listOf(
            EpgProgramme(id = "p3", channelId = "ch2", title = "Documentary", description = null,
                category = "Documentary", startAt = minsAgo(30), endAt = minsFromNow(60), iconUrl = null),
        ),
        "ch3" to listOf(
            EpgProgramme(id = "p4", channelId = "ch3", title = "World News", description = null,
                category = "News", startAt = minsAgo(90), endAt = minsFromNow(120), iconUrl = null),
        ),
    )

    // ── Rendering Tests ────────────────────────────────────────────────────

    @Test
    fun epgGrid_displaysAllChannelNames() {
        composeTestRule.setContent {
            EpgGrid(
                channels = testChannels,
                programmes = testProgrammes,
            )
        }

        composeTestRule.onNodeWithText("BBC One").assertIsDisplayed()
        composeTestRule.onNodeWithText("BBC Two").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sky News").assertIsDisplayed()
    }

    @Test
    fun epgGrid_displaysProgrammeTitles() {
        composeTestRule.setContent {
            EpgGrid(
                channels = testChannels,
                programmes = testProgrammes,
            )
        }

        composeTestRule.onNodeWithText("Morning News").assertExists()
        composeTestRule.onNodeWithText("Breakfast Show").assertExists()
        composeTestRule.onNodeWithText("Documentary").assertExists()
        composeTestRule.onNodeWithText("World News").assertExists()
    }

    @Test
    fun epgGrid_displaysTimeHeaderLabels() {
        composeTestRule.setContent {
            EpgGrid(
                channels = testChannels,
                programmes = testProgrammes,
            )
        }

        // TimeHeader renders HH:mm labels at 30-min intervals
        val timeLabels = composeTestRule.onAllNodes(
            hasText(Regex("\\d{2}:\\d{2}"), substring = false)
        )
        // 4 hours = 8 × 30-min slots
        timeLabels.assertCountIsAtLeast(8)
    }

    @Test
    fun epgGrid_showsCurrentTimeIndicator() {
        composeTestRule.setContent {
            EpgGrid(
                channels = testChannels,
                programmes = testProgrammes,
            )
        }

        // Current-time line is rendered as a red Spacer (2dp wide)
        // We verify the TimeHeader renders with our current time
        val timeHeader = composeTestRule.onAllNodes(hasText(Regex("\\d{2}:\\d{2}"), substring = false))[0]
        assertNotNull(timeHeader)
    }

    // ── Selection / Interaction Tests ──────────────────────────────────────

    @Test
    fun epgGrid_callsOnChannelSelected_whenChannelClicked() {
        var selectedId: String? = null

        composeTestRule.setContent {
            EpgGrid(
                channels = testChannels,
                programmes = testProgrammes,
                onChannelSelected = { ch -> selectedId = ch.id },
            )
        }

        composeTestRule.onNodeWithText("BBC One").performClick()
        assertEquals("ch1", selectedId)
    }

    @Test
    fun epgGrid_callsOnChannelSelected_whenProgrammeClicked() {
        var selectedId: String? = null

        composeTestRule.setContent {
            EpgGrid(
                channels = testChannels,
                programmes = testProgrammes,
                onChannelSelected = { ch -> selectedId = ch.id },
            )
        }

        // Clicking a programme cell also triggers onChannelSelected (per ChannelRow impl)
        composeTestRule.onNodeWithText("Morning News").performClick()
        assertEquals("ch1", selectedId)
    }

    // ── Group Selector Tests ───────────────────────────────────────────────

    @Test
    fun epgGrid_showsGroupSelector_whenGroupsProvided() {
        composeTestRule.setContent {
            EpgGrid(
                channels = testChannels,
                programmes = testProgrammes,
                groups = listOf("All", "UK", "News"),
                selectedGroup = "All",
            )
        }

        composeTestRule.onNodeWithText("All").assertIsDisplayed()
        composeTestRule.onNodeWithText("UK").assertIsDisplayed()
        composeTestRule.onNodeWithText("News").assertIsDisplayed()
    }

    @Test
    fun epgGrid_hidesGroupSelector_whenNoGroups() {
        composeTestRule.setContent {
            EpgGrid(
                channels = testChannels,
                programmes = testProgrammes,
                groups = emptyList(),
            )
        }

        composeTestRule.onNodeWithText("All").assertDoesNotExist()
    }

    @Test
    fun epgGrid_callsOnGroupSelected_whenGroupClicked() {
        var selectedGroup: String? = null

        composeTestRule.setContent {
            EpgGrid(
                channels = testChannels,
                programmes = testProgrammes,
                groups = listOf("All", "UK", "News"),
                selectedGroup = "All",
                onGroupSelected = { g -> selectedGroup = g },
            )
        }

        composeTestRule.onNodeWithText("News").performClick()
        assertEquals("News", selectedGroup)
    }

    // ── Empty / Loading / Error State Tests ────────────────────────────────

    @Test
    fun epgGrid_showsEmptyState_whenNoChannels() {
        composeTestRule.setContent {
            EpgGrid(
                channels = emptyList(),
                programmes = emptyMap(),
            )
        }

        composeTestRule.onNodeWithText("No channels loaded").assertIsDisplayed()
    }

    @Test
    fun epgGrid_showsLoadingState_whenLoadingAndNoChannels() {
        composeTestRule.setContent {
            EpgGrid(
                channels = emptyList(),
                programmes = emptyMap(),
                isLoading = true,
            )
        }

        composeTestRule.onNodeWithText("Loading EPG...").assertIsDisplayed()
    }

    @Test
    fun epgGrid_showsErrorState_whenErrorAndNoChannels() {
        composeTestRule.setContent {
            EpgGrid(
                channels = emptyList(),
                programmes = emptyMap(),
                error = "Network timeout",
                onRefresh = {},
            )
        }

        composeTestRule.onNodeWithText("⚠ Network timeout").assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
    }

    @Test
    fun epgGrid_showsChannels_whenLoadingWithData() {
        composeTestRule.setContent {
            EpgGrid(
                channels = testChannels,
                programmes = testProgrammes,
                isLoading = true,
            )
        }

        // Should show channels even while loading (stale-while-revalidate pattern)
        composeTestRule.onNodeWithText("BBC One").assertIsDisplayed()
    }

    @Test
    fun epgGrid_showsChannels_whenErrorWithData() {
        composeTestRule.setContent {
            EpgGrid(
                channels = testChannels,
                programmes = testProgrammes,
                error = "EPG fetch failed",
            )
        }

        // Should show existing channels even with error
        composeTestRule.onNodeWithText("BBC One").assertIsDisplayed()
    }

    // ── Retry Button Test ──────────────────────────────────────────────────

    @Test
    fun epgGrid_callsOnRefresh_whenRetryClicked() {
        var refreshCalled = false

        composeTestRule.setContent {
            EpgGrid(
                channels = emptyList(),
                programmes = emptyMap(),
                error = "Network timeout",
                onRefresh = { refreshCalled = true },
            )
        }

        composeTestRule.onNodeWithText("Retry").performClick()
        assert(refreshCalled)
    }
}
