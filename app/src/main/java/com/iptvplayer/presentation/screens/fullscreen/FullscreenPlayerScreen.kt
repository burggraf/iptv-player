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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.iptvplayer.domain.model.Channel
import com.iptvplayer.domain.model.PlaybackState
import com.iptvplayer.presentation.theme.AppColors
import kotlinx.coroutines.delay

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FullscreenPlayerScreen(
    player: Player,
    channel: Channel?,
    playbackState: PlaybackState = PlaybackState.Idle,
    numberBuffer: String = "",
    onNumberInput: (String) -> Unit = {},
    onSwitchChannel: (Int) -> Unit = {},
    onSwitchChannelInGroup: (Int) -> Unit = {},
    onBack: () -> Unit,
    onToggleFullscreen: () -> Unit = {},
) {
    var showControls by remember { mutableStateOf(true) }

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
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false

                when (keyEvent.key) {
                    Key.Zero -> { onNumberInput("0"); true }
                    Key.One -> { onNumberInput("1"); true }
                    Key.Two -> { onNumberInput("2"); true }
                    Key.Three -> { onNumberInput("3"); true }
                    Key.Four -> { onNumberInput("4"); true }
                    Key.Five -> { onNumberInput("5"); true }
                    Key.Six -> { onNumberInput("6"); true }
                    Key.Seven -> { onNumberInput("7"); true }
                    Key.Eight -> { onNumberInput("8"); true }
                    Key.Nine -> { onNumberInput("9"); true }

                    Key.DirectionUp -> {
                        onSwitchChannelInGroup(-1)
                        showControls = true
                        true
                    }
                    Key.DirectionDown -> {
                        onSwitchChannelInGroup(1)
                        showControls = true
                        true
                    }

                    Key.DirectionLeft -> {
                        showControls = true
                        false
                    }
                    Key.DirectionRight -> {
                        showControls = true
                        false
                    }

                    Key.Enter, Key.NumPadEnter -> {
                        showControls = !showControls
                        true
                    }

                    Key.Back, Key.Escape -> {
                        onBack()
                        true
                    }

                    else -> false
                }
            }
    ) {
        AndroidView(
            factory = { context ->
                PlayerView(context).apply {
                    this.player = player
                    useController = false
                    setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    keepScreenOn = true
                    isFocusable = true
                }
            },
            update = { it.player = player },
            modifier = Modifier.fillMaxSize(),
        )

        // Top bar with channel info
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopStart),
        ) {
            Row(
                modifier = Modifier
                    .padding(20.dp)
                    .background(Color(0xFF000000).copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to EPG",
                        tint = Color.White,
                    )
                }
                channel?.let { ch ->
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text(
                            text = ch.name,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        ch.group?.let { group ->
                            Text(
                                text = "$group  •  Channel ${ch.number}",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 12.sp,
                            )
                        }
                    }
                }
            }
        }

        // Number buffer display
        AnimatedVisibility(
            visible = numberBuffer.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopEnd),
        ) {
            Box(
                modifier = Modifier
                    .padding(20.dp)
                    .background(Color(0xFF000000).copy(alpha = 0.7f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 28.dp, vertical = 14.dp),
            ) {
                Text(
                    text = numberBuffer,
                    color = AppColors.Primary,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        // Loading indicator
        AnimatedVisibility(
            visible = (playbackState is PlaybackState.Loading || playbackState is PlaybackState.Buffering) && !showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.Center)
                .background(Color(0xFF000000).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .padding(16.dp),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(
                    modifier = Modifier.size(40.dp),
                    color = AppColors.Primary,
                    strokeWidth = 3.dp,
                )
                Text(
                    text = if (playbackState is PlaybackState.Buffering) "Buffering..." else "Loading...",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}

private const val CONTROLS_TIMEOUT_MS = 4_000L
