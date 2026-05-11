package com.iptvplayer.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Surface

/**
 * EPG grid component — placeholder.
 * Phase 4 — implement synced channel×time grid with DPad navigation.
 */
@Composable
fun EpgGrid(
    modifier: Modifier = Modifier,
    // TODO: channels, programmes, currentTime, scroll states, callbacks
) {
    Surface(modifier = modifier.padding(16.dp)) {
        Box {
            Text("EPG Grid — Coming in Phase 4")
        }
    }
}
