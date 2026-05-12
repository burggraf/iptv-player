package com.iptvplayer.presentation.screens.epg

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iptvplayer.core.NetworkMonitor
import com.iptvplayer.presentation.components.EpgGrid
import com.iptvplayer.presentation.components.EpgSkeletonLoader
import com.iptvplayer.presentation.components.OfflineBanner
import com.iptvplayer.presentation.components.SearchBar
import com.iptvplayer.presentation.components.VideoPreview
import com.iptvplayer.presentation.viewmodel.EpgViewModel
import com.iptvplayer.presentation.viewmodel.PlayerViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.java.KoinJavaComponent.getKoin

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

    val networkMonitor: NetworkMonitor = getKoin().get()
    val isOnline by networkMonitor.isOnline.collectAsStateWithLifecycle(true)

    // Sync channels to PlayerViewModel for quick channel switching
    LaunchedEffect(uiState.channels) {
        playerViewModel.setChannels(uiState.channels)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        VideoPreview(
            player = playerViewModel.player,
            modifier = Modifier.weight(0.3f),
            channel = currentChannel,
            playbackState = playbackState,
            onClick = { onNavigateToFullscreen() },
            onRetry = {
                currentChannel?.let { playerViewModel.selectChannel(it) }
            },
        )

        OfflineBanner(
            isOffline = !isOnline,
            onRetry = { /* retry network check handled by NetworkMonitor */ },
        )

        if (uiState.isLoading && uiState.channels.isEmpty()) {
            EpgSkeletonLoader()
        } else {
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = { epgViewModel.setSearchQuery(it) },
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
}
