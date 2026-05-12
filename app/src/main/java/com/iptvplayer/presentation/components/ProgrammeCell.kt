package com.iptvplayer.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Surface
import com.iptvplayer.domain.model.EpgProgramme
import com.iptvplayer.presentation.theme.AppColors
import java.time.Instant

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
                        color = if (isNowPlaying) AppColors.EpgProgrammeNow else AppColors.EpgProgrammeBg,
                        shape = RoundedCornerShape(4.dp),
                    )
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                if (isNowPlaying) {
                    Text(
                        text = "NOW",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier
                            .background(AppColors.EpgNowPlayingBadge, RoundedCornerShape(3.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }

                Text(
                    text = programme.title,
                    fontSize = 11.sp,
                    color = if (isNowPlaying) Color.White else AppColors.TextSecondary,
                    fontWeight = if (isNowPlaying) FontWeight.Medium else FontWeight.Normal,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                programme.category?.let { category ->
                    Text(
                        text = category,
                        fontSize = 9.sp,
                        color = AppColors.TextTertiary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (isNowPlaying && progress > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(2.dp)
                            .background(AppColors.EpgNowPlayingBadge, RoundedCornerShape(1.dp))
                    )
                }
            }
        }
    }
}
