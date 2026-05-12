package com.iptvplayer.presentation.screens.epg

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iptvplayer.presentation.components.EpgGrid
import com.iptvplayer.presentation.components.VideoPreview
import com.iptvplayer.presentation.viewmodel.EpgViewModel
import com.iptvplayer.presentation.viewmodel.PlayerViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun EpgScreen(
    playerViewModel: PlayerViewModel = koinViewModel(),
    epgViewModel: EpgViewModel = koinViewModel(),
    onNavigateToFullscreen: () -> Unit,
    onBack: () -> Unit,
) {
    val uiState by epgViewModel.uiState.collectAsStateWithLifecycle()
    val currentChannel by playerViewModel.currentChannel.collectAsStateWithLifecycle()
    val playbackState by playerViewModel.playbackState.collectAsStateWithLifecycle()

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
            channels = uiState.filteredChannels,
            programmes = uiState.programmes,
            groups = uiState.groups,
            selectedGroup = uiState.selectedGroup,
            isLoading = uiState.isLoading,
            error = uiState.error,
            onChannelSelected = { channel ->
                playerViewModel.selectChannel(channel)
            },
            onGroupSelected = { group ->
                epgViewModel.selectGroup(group)
            },
            onRefresh = {
                epgViewModel.fetchEpg(emptyList())
            },
        )
    }
}
