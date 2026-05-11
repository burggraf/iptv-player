package com.iptvplayer.presentation.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Surface

/**
 * Channel row in EPG grid — placeholder.
 * Phase 4 — implement with programme cells and horizontal scrolling.
 */
@Composable
fun ChannelRow(
    modifier: Modifier = Modifier,
    channelName: String = "",
) {
    Surface(modifier = modifier.padding(vertical = 4.dp)) {
        Row(modifier = Modifier.padding(8.dp)) {
            Text(channelName)
        }
    }
}
