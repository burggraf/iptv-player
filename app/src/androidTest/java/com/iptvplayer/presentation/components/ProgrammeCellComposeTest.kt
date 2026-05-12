package com.iptvplayer.presentation.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.iptvplayer.domain.model.EpgProgramme
import org.junit.Rule
import org.junit.Test
import java.time.Instant

/**
 * Phase 7 — Compose UI Tests for ProgrammeCell and TimeHeader.
 */
class ProgrammeCellComposeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val now = Instant.now()
    private val startTime = now.minusSeconds(3600) // 1 hour ago

    private fun minsAgo(m: Long) = now.minusSeconds(m * 60)
    private fun minsFromNow(m: Long) = now.plusSeconds(m * 60)

    @Test
    fun programmeCell_showsTitle() {
        val programme = EpgProgramme(
            id = "p1", channelId = "ch1", title = "Test Show",
            startAt = minsAgo(30), endAt = minsFromNow(30),
        )

        composeTestRule.setContent {
            ProgrammeCell(
                programme = programme,
                startTime = startTime,
                currentTime = now,
                onClick = {},
            )
        }

        composeTestRule.onNodeWithText("Test Show").assertExists()
    }

    @Test
    fun programmeCell_showsCategory() {
        val programme = EpgProgramme(
            id = "p1", channelId = "ch1", title = "News",
            category = "Breaking News",
            startAt = minsAgo(30), endAt = minsFromNow(30),
        )

        composeTestRule.setContent {
            ProgrammeCell(
                programme = programme,
                startTime = startTime,
                currentTime = now,
                onClick = {},
            )
        }

        composeTestRule.onNodeWithText("Breaking News").assertExists()
    }

    @Test
    fun programmeCell_showsNowBadge_whenCurrentlyPlaying() {
        val programme = EpgProgramme(
            id = "p1", channelId = "ch1", title = "Live Show",
            startAt = minsAgo(30), endAt = minsFromNow(30),
        )

        composeTestRule.setContent {
            ProgrammeCell(
                programme = programme,
                startTime = startTime,
                currentTime = now,
                onClick = {},
            )
        }

        composeTestRule.onNodeWithText("NOW").assertExists()
    }

    @Test
    fun programmeCell_noNowBadge_whenPastProgramme() {
        val programme = EpgProgramme(
            id = "p1", channelId = "ch1", title = "Old Show",
            startAt = minsAgo(120), endAt = minsAgo(60),
        )

        composeTestRule.setContent {
            ProgrammeCell(
                programme = programme,
                startTime = startTime,
                currentTime = now,
                onClick = {},
            )
        }

        composeTestRule.onNodeWithText("NOW").assertDoesNotExist()
    }

    @Test
    fun programmeCell_noNowBadge_whenFutureProgramme() {
        val programme = EpgProgramme(
            id = "p1", channelId = "ch1", title = "Future Show",
            startAt = minsFromNow(60), endAt = minsFromNow(120),
        )

        composeTestRule.setContent {
            ProgrammeCell(
                programme = programme,
                startTime = startTime,
                currentTime = now,
                onClick = {},
            )
        }

        composeTestRule.onNodeWithText("NOW").assertDoesNotExist()
    }

    @Test
    fun programmeCell_callsOnClick_whenClicked() {
        var clicked = false
        val programme = EpgProgramme(
            id = "p1", channelId = "ch1", title = "Clickable Show",
            startAt = minsAgo(30), endAt = minsFromNow(30),
        )

        composeTestRule.setContent {
            ProgrammeCell(
                programme = programme,
                startTime = startTime,
                currentTime = now,
                onClick = { clicked = true },
            )
        }

        composeTestRule.onNodeWithText("Clickable Show").performClick()
        assert(clicked)
    }

    @Test
    fun programmeCell_widthProportionalToDuration() {
        val short = EpgProgramme(
            id = "ps", channelId = "ch1", title = "Short",
            startAt = minsAgo(15), endAt = minsFromNow(15),
        )
        val long = EpgProgramme(
            id = "pl", channelId = "ch1", title = "Long",
            startAt = minsAgo(60), endAt = minsFromNow(60),
        )

        composeTestRule.setContent {
            ProgrammeCell(
                programme = short,
                startTime = startTime,
                currentTime = now,
                pixelPerMinute = 2f,
                onClick = {},
            )
        }
        val shortNode = composeTestRule.onNodeWithText("Short").fetchSemanticsNode()
        val shortWidth = shortNode.layoutInfo.size.width

        composeTestRule.setContent {
            ProgrammeCell(
                programme = long,
                startTime = startTime,
                currentTime = now,
                pixelPerMinute = 2f,
                onClick = {},
            )
        }
        val longNode = composeTestRule.onNodeWithText("Long").fetchSemanticsNode()
        val longWidth = longNode.layoutInfo.size.width

        // Long (120 min) should be ~2x wider than Short (30 min)
        val ratio = longWidth.toDouble() / shortWidth.toDouble()
        assert(ratio > 1.5 && ratio < 2.5) { "Width ratio expected ~2.0, got $ratio" }
    }
}
