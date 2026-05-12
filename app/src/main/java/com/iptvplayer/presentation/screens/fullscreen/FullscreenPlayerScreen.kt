package com.iptvplayer.presentation.screens.fullscreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.tv.material3.LocalContentColor
import com.iptvplayer.domain.model.Channel
import com.iptvplayer.domain.model.PlaybackState
import kotlinx.coroutines.delay

/**
 * Fullscreen player screen — Media3 PlayerView with full transport controls.
 * Controls auto-hide after 4s of inactivity. DPad navigable.
 */
@Composable
fun FullscreenPlayerScreen(
    player: Player,
    channel: Channel?,
    playbackState: PlaybackState = PlaybackState.Idle,
    onBack: () -> Unit,
    onToggleFullscreen: () -> Unit = {},
) {
    var showControls by remember { mutableStateOf(true) }

    // Auto-hide controls after inactivity
    LaunchedEffect(showControls) {
        if (showControls) {
            delay(CONTROLS_TIMEOUT_MS)
            showControls = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Video surface with controls
        AndroidView(
            factory = { context ->
                PlayerView(context).apply {
                    this.player = player
                    useController = true
                    controllerShowTimeoutMs = CONTROLS_TIMEOUT_MS.toInt()
                    setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    keepScreenOn = true
                    // Enable DPad navigation
                    isFocusable = true
                    setOnClickListener {
                        // Toggle controls visibility on click
                        showControls = !showControls
                    }
                }
            },
            update = { view ->
                view.player = player
            },
            modifier = Modifier.fillMaxSize()
        )

        // Top bar with back button
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to EPG",
                        tint = Color.White
                    )
                }

                channel?.let { ch ->
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text(
                            text = ch.name,
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium
                        )
                        ch.group?.let { group ->
                            Text(
                                text = "$group • Channel ${ch.number}",
                                color = Color.White.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }

        // Loading indicator (overlay when controls hidden)
        AnimatedVisibility(
            visible = (playbackState is PlaybackState.Loading || playbackState is PlaybackState.Buffering) && !showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.Center)
                .background(Color.Black.copy(alpha = 0.4f))
                .padding(12.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(40.dp),
                color = Color.White,
                strokeWidth = 3.dp
            )
        }
    }
}

private const val CONTROLS_TIMEOUT_MS = 4_000L
