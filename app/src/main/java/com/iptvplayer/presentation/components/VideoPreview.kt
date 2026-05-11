package com.iptvplayer.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Surface

/**
 * Video preview component — placeholder for Media3 StyledPlayerView.
 * Phase 3 — integrate ExoPlayer surface.
 */
@Composable
fun VideoPreview(
    modifier: Modifier = Modifier,
    channelName: String? = null,
    onClick: () -> Unit = {},
) {
    Surface(
        onClick = onClick,
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(200.dp, 120.dp)
        ) {
            if (channelName != null) {
                Text(channelName)
            } else {
                CircularProgressIndicator()
            }
        }
    }
}
