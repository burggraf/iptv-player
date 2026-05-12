package com.iptvplayer.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Surface
import com.iptvplayer.domain.model.Channel

/**
 * EPG grid component — placeholder.
 * Phase 4 — implement synced channel×time grid with DPad navigation.
 */
@Composable
fun EpgGrid(
    modifier: Modifier = Modifier,
    channels: List<Channel> = emptyList(),
    onChannelSelected: (Channel) -> Unit = {},
) {
    Surface(modifier = modifier.padding(16.dp)) {
        Box {
            if (channels.isEmpty()) {
                Text("EPG Grid — Load a playlist to see channels. Phase 4 will implement the full grid.")
            }
        }
    }
}
