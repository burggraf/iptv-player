package com.iptvplayer.presentation.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Time header for EPG grid — placeholder.
 * Phase 4 — implement fixed-width time slots with current-time indicator.
 */
@Composable
fun TimeHeader(
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
        Text("18:00    18:30    19:00    19:30    20:00")
    }
}
