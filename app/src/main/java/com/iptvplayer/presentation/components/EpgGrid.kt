package com.iptvplayer.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iptvplayer.domain.model.Channel
import com.iptvplayer.domain.model.EpgProgramme
import java.time.Instant
import java.time.ZoneId
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

/**
 * Full EPG grid: time header + scrollable channel rows with programme cells.
 * Synced horizontal scroll between header and rows.
 */
@Composable
fun EpgGrid(
    channels: List<Channel>,
    programmes: Map<String, List<EpgProgramme>>,
    groups: List<String> = emptyList(),
    selectedGroup: String = EpgGridDefaults.GROUP_ALL,
    isLoading: Boolean = false,
    error: String? = null,
    modifier: Modifier = Modifier,
    channelNameWidth: Dp = EpgGridDefaults.CHANNEL_NAME_WIDTH,
    timeWindow: Duration = EpgGridDefaults.TIME_WINDOW,
    pixelPerMinute: Float = EpgGridDefaults.PIXEL_PER_MINUTE,
    onChannelSelected: (Channel) -> Unit = {},
    onGroupSelected: (String) -> Unit = {},
    onRefresh: () -> Unit = {},
) {
    if (isLoading && channels.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Color.White)
                Text("Loading EPG...", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))
            }
        }
        return
    }

    if (error != null && channels.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("⚠ $error", color = Color(0xFFFF6B6B), fontSize = 14.sp)
                androidx.tv.material3.Button(
                    onClick = onRefresh,
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Text("Retry")
                }
            }
        }
        return
    }

    if (channels.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No channels loaded", color = Color.White.copy(alpha = 0.5f), fontSize = 16.sp)
        }
        return
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Group selector
        if (groups.isNotEmpty()) {
            GroupSelector(
                groups = groups,
                selectedGroup = selectedGroup,
                onGroupSelected = onGroupSelected,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        EpgGridContent(
            channels = channels,
            programmes = programmes,
            channelNameWidth = channelNameWidth,
            timeWindow = timeWindow,
            pixelPerMinute = pixelPerMinute,
            onChannelSelected = onChannelSelected,
        )
    }
}

@Composable
private fun EpgGridContent(
    channels: List<Channel>,
    programmes: Map<String, List<EpgProgramme>>,
    channelNameWidth: Dp,
    timeWindow: Duration,
    pixelPerMinute: Float,
    onChannelSelected: (Channel) -> Unit,
) {
    val now = Instant.now()
    val startTime = now.atZone(ZoneId.systemDefault())
        .withMinute(0).withSecond(0).withNano(0).toInstant()
    val endTime = startTime.plusSeconds(timeWindow.inWholeSeconds)

    val horizontalScrollState = rememberScrollState()

    // Scroll to current time on first composition
    val density = LocalDensity.current
    val channelNamePx = remember(channelNameWidth, density) {
        with(density) { channelNameWidth.toPx() }
    }
    var hasScrolledToNow by remember { mutableStateOf(false) }
    val currentMinutesFromStart = (now.epochSecond - startTime.epochSecond) / 60
    val currentOffsetPx = channelNamePx + (currentMinutesFromStart * pixelPerMinute)

    LaunchedEffect(currentOffsetPx) {
        if (!hasScrolledToNow && currentOffsetPx > channelNamePx) {
            horizontalScrollState.scrollTo(currentOffsetPx.toInt().coerceAtLeast(0))
            hasScrolledToNow = true
        }
    }

    Column {
        // Time header
        TimeHeader(
            startTime = startTime,
            endTime = endTime,
            currentTime = now,
            channelNameWidth = channelNameWidth,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1A1A2E))
        )

        // Channel rows
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(channels, key = { it.id }) { channel ->
                ChannelRow(
                    channel = channel,
                    programmes = programmes[channel.id] ?: emptyList(),
                    startTime = startTime,
                    endTime = endTime,
                    currentTime = now,
                    channelNameWidth = channelNameWidth,
                    pixelPerMinute = pixelPerMinute,
                    horizontalScrollState = horizontalScrollState,
                    onChannelSelected = { onChannelSelected(channel) },
                )
            }
        }
    }
}

/**
 * Group selector pills.
 */
@Composable
private fun GroupSelector(
    groups: List<String>,
    selectedGroup: String,
    onGroupSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically
    ) {
        groups.forEach { group ->
            val isSelected = group == selectedGroup
            Box(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .background(
                        if (isSelected) Color(0xFF1E3A5F) else Color(0xFF1A1A2E)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .clickable { onGroupSelected(group) },
            ) {
                Text(
                    text = group,
                    fontSize = 13.sp,
                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

object EpgGridDefaults {
    val CHANNEL_NAME_WIDTH = 100.dp
    val TIME_WINDOW = 4.hours
    const val PIXEL_PER_MINUTE = 2f
    const val GROUP_ALL = "All"
}
