package com.iptvplayer.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Surface
import com.iptvplayer.domain.model.Channel
import com.iptvplayer.domain.model.EpgProgramme
import java.time.Instant

/**
 * Single channel row in the EPG grid.
 * Left: channel name column. Right: horizontally scrollable programme cells.
 */
@Composable
fun ChannelRow(
    channel: Channel,
    programmes: List<EpgProgramme>,
    startTime: Instant,
    endTime: Instant,
    currentTime: Instant,
    channelNameWidth: Dp = 100.dp,
    pixelPerMinute: Float = 2f,
    horizontalScrollState: androidx.compose.foundation.ScrollState,
    onChannelSelected: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .height(56.dp)
            .background(Color(0xFF16213E))
    ) {
        // Channel name column
        Surface(
            onClick = onChannelSelected,
            modifier = Modifier
                .width(channelNameWidth)
                .fillMaxHeight()
                .background(Color(0xFF0F3460))
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = channel.name,
                    fontSize = 13.sp,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Programme cells (horizontal scroll synced with header)
        Box(
            modifier = Modifier
                .horizontalScroll(horizontalScrollState)
                .fillMaxHeight()
        ) {
            // Total timeline width
            val totalMinutes = (endTime.epochSecond - startTime.epochSecond) / 60
            val totalWidthDp = (totalMinutes * pixelPerMinute).dp

            Spacer(
                modifier = Modifier
                    .width(totalWidthDp)
                    .fillMaxHeight()
            )

            // Programme cells positioned on the timeline
            programmes.forEach { programme ->
                ProgrammeCell(
                    programme = programme,
                    startTime = startTime,
                    currentTime = currentTime,
                    pixelPerMinute = pixelPerMinute,
                    onClick = { onChannelSelected() },
                )
            }

            // Current-time indicator line
            val minutesFromStart = (currentTime.epochSecond - startTime.epochSecond) / 60
            val lineOffsetDp = (minutesFromStart * pixelPerMinute).dp
            if (minutesFromStart >= 0) {
                Spacer(
                    modifier = Modifier
                        .offset { androidx.compose.ui.unit.IntOffset(lineOffsetDp.value.toInt(), 0) }
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(Color(0xFFFF3B30).copy(alpha = 0.6f))
                )
            }
        }
    }
}
