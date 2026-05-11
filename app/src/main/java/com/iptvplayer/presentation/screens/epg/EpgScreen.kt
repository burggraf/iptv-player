package com.iptvplayer.presentation.screens.epg

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.iptvplayer.presentation.components.EpgGrid
import com.iptvplayer.presentation.components.VideoPreview

@Composable
fun EpgScreen(
    onNavigateToFullscreen: (String) -> Unit,
    onBack: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        VideoPreview(
            modifier = Modifier.weight(0.3f),
            channelName = "No channel selected",
            onClick = { onNavigateToFullscreen("") }
        )
        EpgGrid(modifier = Modifier.weight(0.7f))
    }
}
