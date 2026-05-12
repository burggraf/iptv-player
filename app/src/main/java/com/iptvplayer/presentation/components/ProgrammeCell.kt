package com.iptvplayer.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import com.iptvplayer.domain.model.EpgProgramme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Single programme cell in the EPG grid.
 * Width derived from programme duration × pixelPerMinute.
 * Shows NOW badge + progress bar for currently airing programmes.
 */
@Composable
fun ProgrammeCell(
    programme: EpgProgramme,
    startTime: Instant,
    currentTime: Instant,
    pixelPerMinute: Float = 2f,
    onClick: () -> Unit = {},
) {
    val durationMinutes = (programme.endAt.epochSecond - programme.startAt.epochSecond) / 60
    val widthDp = (durationMinutes * pixelPerMinute).dp

    val isNowPlaying = programme.startAt <= currentTime && programme.endAt > currentTime

    // Calculate progress for now-playing programmes
    val progress = if (isNowPlaying) {
        val totalDuration = programme.endAt.epochSecond - programme.startAt.epochSecond
        val elapsed = currentTime.epochSecond - programme.startAt.epochSecond
        (elapsed.toFloat() / totalDuration.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    val leftOffsetDp = ((programme.startAt.epochSecond - startTime.epochSecond) / 60 * pixelPerMinute).dp

    Box(
        modifier = Modifier
            .offset { androidx.compose.ui.unit.IntOffset(leftOffsetDp.value.toInt(), 0) }
            .width(widthDp)
            .height(56.dp)
            .padding(1.dp)
    ) {
        Surface(
            onClick = onClick,
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (isNowPlaying) Color(0xFF1A3A5C) else Color(0xFF1A1A2E)
                    )
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                // NOW badge
                if (isNowPlaying) {
                    Text(
                        text = "NOW",
                        fontSize = 8.sp,
                        color = Color.White,
                        modifier = Modifier
                            .background(Color(0xFFE94560))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }

                // Programme title
                Text(
                    text = programme.title,
                    fontSize = 11.sp,
                    color = if (isNowPlaying) Color.White else Color.White.copy(alpha = 0.8f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                // Category or description
                programme.category?.let { category ->
                    Text(
                        text = category,
                        fontSize = 9.sp,
                        color = Color.White.copy(alpha = 0.5f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Progress bar for now-playing
                if (isNowPlaying && progress > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(2.dp)
                            .background(Color(0xFFE94560))
                    )
                }
            }
        }
    }
}
