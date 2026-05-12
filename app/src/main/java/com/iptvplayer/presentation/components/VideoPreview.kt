package com.iptvplayer.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.tv.material3.Surface
import com.iptvplayer.domain.model.Channel
import com.iptvplayer.domain.model.PlaybackState

/**
 * Video preview component — Media3 PlayerView embedded via AndroidView.
 * No transport controls. Shows channel overlay + loading/error states.
 */
@Composable
fun VideoPreview(
    player: Player,
    modifier: Modifier = Modifier,
    channel: Channel? = null,
    playbackState: PlaybackState = PlaybackState.Idle,
    onClick: () -> Unit = {},
) {
    Surface(
        onClick = onClick,
        modifier = modifier
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Video surface
            AndroidView(
                factory = { context ->
                    PlayerView(context).apply {
                        this.player = player
                        useController = false
                        setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER) // We handle overlay
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                        keepScreenOn = true
                    }
                },
                update = { view ->
                    view.player = player
                },
                modifier = Modifier.fillMaxSize()
            )

            // Loading overlay
            AnimatedVisibility(
                visible = playbackState is PlaybackState.Loading || playbackState is PlaybackState.Buffering,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = Color.White,
                        strokeWidth = 3.dp
                    )
                    Text(
                        text = if (playbackState is PlaybackState.Buffering) "Buffering..." else "Loading...",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            // Error overlay
            AnimatedVisibility(
                visible = playbackState is PlaybackState.Error,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(16.dp)
            ) {
                val error = playbackState as PlaybackState.Error
                Text(
                    text = "⚠ ${error.message}",
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Channel overlay (bottom-left)
            AnimatedVisibility(
                visible = playbackState !is PlaybackState.Error && channel != null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.BottomStart)
            ) {
                if (channel != null) {
                    ChannelOverlay(channel = channel)
                }
            }

            // Idle state placeholder
            AnimatedVisibility(
                visible = playbackState is PlaybackState.Idle && channel == null,
                modifier = Modifier.align(Alignment.Center)
            ) {
                Text(
                    text = "Select a channel to play",
                    color = Color.White.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/**
 * Channel name + group overlay on video preview.
 */
@Composable
private fun ChannelOverlay(
    channel: Channel,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.6f))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column {
            Text(
                text = channel.name,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium
            )
            channel.group?.let { group ->
                Text(
                    text = group,
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
