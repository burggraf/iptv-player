package com.iptvplayer.presentation.screens.epg

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.iptvplayer.domain.model.PlaybackState
import com.iptvplayer.presentation.components.EpgGrid
import com.iptvplayer.presentation.components.VideoPreview
import com.iptvplayer.presentation.viewmodel.PlayerViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun EpgScreen(
    playerViewModel: PlayerViewModel = koinViewModel(),
    onNavigateToFullscreen: () -> Unit,
    onBack: () -> Unit,
) {
    val currentChannel by playerViewModel.currentChannel.collectAsState()
    val playbackState by playerViewModel.playbackState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        VideoPreview(
            player = playerViewModel.player,
            modifier = Modifier.weight(0.3f),
            channel = currentChannel,
            playbackState = playbackState,
            onClick = { onNavigateToFullscreen() }
        )
        EpgGrid(
            modifier = Modifier.weight(0.7f),
            onChannelSelected = { channel ->
                playerViewModel.selectChannel(channel)
            },
        )
    }
}
