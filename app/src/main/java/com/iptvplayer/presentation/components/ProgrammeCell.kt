package com.iptvplayer.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Surface

/**
 * Programme cell in EPG grid — placeholder.
 * Phase 4 — implement with width based on duration, NOW badge, progress bar.
 */
@Composable
fun ProgrammeCell(
    modifier: Modifier = Modifier,
    programmeTitle: String = "",
    isSelected: Boolean = false,
    onSelected: () -> Unit = {},
) {
    Surface(
        onClick = onSelected,
        modifier = modifier
    ) {
        Box(modifier = Modifier.padding(8.dp)) {
            Text(programmeTitle)
        }
    }
}
