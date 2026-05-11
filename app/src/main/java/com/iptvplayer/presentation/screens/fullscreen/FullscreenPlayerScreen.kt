package com.iptvplayer.presentation.screens.fullscreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.tv.material3.Button

/**
 * Fullscreen player screen — placeholder.
 * Phase 3 — integrate Media3 StyledPlayerView with full controls.
 */
@Composable
fun FullscreenPlayerScreen(
    onBack: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Fullscreen Player — Phase 3")
        Button(
            onClick = onBack,
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Text("Back")
        }
    }
}
