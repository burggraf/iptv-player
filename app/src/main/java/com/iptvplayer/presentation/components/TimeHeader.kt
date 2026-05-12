package com.iptvplayer.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Pixel width per minute of EPG timeline.
 * 2px/min → 60px per 30min slot, 120px per hour.
 */
const val PIXELS_PER_MINUTE = 2f

/**
 * Time header showing fixed-width time labels synced with programme cells.
 * Red current-time line overlay.
 */
@Composable
fun TimeHeader(
    startTime: Instant,
    endTime: Instant,
    currentTime: Instant,
    modifier: Modifier = Modifier,
    channelNameWidth: Dp = 100.dp,
) {
    val density = LocalDensity.current
    val channelNamePx = remember(channelNameWidth, density) {
        with(density) { channelNameWidth.toPx() }
    }

    Box(modifier = modifier.height(36.dp)) {
        Row(
            modifier = Modifier.align(Alignment.CenterStart),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Spacer for channel name column
            Spacer(modifier = Modifier.width(channelNameWidth))

            // Time labels at 30-min intervals
            val totalMinutes = (endTime.epochSecond - startTime.epochSecond) / 60
            var elapsedMinutes = 0
            while (elapsedMinutes < totalMinutes) {
                val time = startTime.plusSeconds((elapsedMinutes * 60).toLong())
                val slotWidth = (30 * PIXELS_PER_MINUTE).dp
                val label = DateTimeFormatter
                    .ofPattern("HH:mm")
                    .withZone(ZoneId.systemDefault())
                    .format(time)

                Text(
                    text = label,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Start,
                    modifier = Modifier.width(slotWidth)
                )
                elapsedMinutes += 30
            }
        }

        // Current-time indicator line
        val minutesFromStart = (currentTime.epochSecond - startTime.epochSecond) / 60
        val lineOffsetPx = channelNamePx + (minutesFromStart * PIXELS_PER_MINUTE)
        val lineOffsetDp = remember(lineOffsetPx, density) {
            with(density) { lineOffsetPx.toDp() }
        }

        if (lineOffsetPx > channelNamePx) {
            Spacer(
                modifier = Modifier
                    .offset { IntOffset(lineOffsetDp.roundToPx(), 0) }
                    .width(2.dp)
                    .height(36.dp)
                    .background(Color(0xFFFF3B30))
            )
        }
    }
}
