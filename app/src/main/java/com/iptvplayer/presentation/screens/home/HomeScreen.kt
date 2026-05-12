package com.iptvplayer.presentation.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.Text
import com.iptvplayer.domain.model.Playlist

@Composable
fun HomeScreen(
    playlists: List<Playlist> = emptyList(),
    selectedPlaylistId: String? = null,
    onNavigateToEpg: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToAddPlaylist: () -> Unit = {},
    onNavigateToFavorites: () -> Unit = {},
    onPlaylistSelected: (String) -> Unit = {},
    onPlaylistRemoved: (String) -> Unit = {},
    onPlaylistRefreshed: (String) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 48.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Text("IPTV Player", modifier = Modifier.padding(bottom = 24.dp))

        // Playlist list
        if (playlists.isEmpty()) {
            Button(
                onClick = onNavigateToAddPlaylist,
                modifier = Modifier.padding(bottom = 16.dp),
            ) {
                Text("Add Playlist")
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .padding(bottom = 24.dp),
            ) {
                playlists.forEach { playlist ->
                    val selected = playlist.id == selectedPlaylistId
                    val label = buildString {
                        append(playlist.name)
                        append(" (")
                        append(playlist.type.name)
                        append(")")
                        if (selected) append(" \u2713")
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Button(
                            onClick = { onPlaylistSelected(playlist.id) },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(label)
                        }
                        Button(
                            onClick = { onPlaylistRefreshed(playlist.id) },
                        ) {
                            Text("\u21BB")
                        }
                        Button(
                            onClick = { onPlaylistRemoved(playlist.id) },
                        ) {
                            Text("\u2715")
                        }
                    }
                }
            }

            Button(
                onClick = onNavigateToAddPlaylist,
                modifier = Modifier.padding(bottom = 16.dp),
            ) {
                Text("+ Add Playlist")
            }
        }

        // Navigation buttons
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(onClick = onNavigateToEpg) {
                Text("Open EPG")
            }
            Button(onClick = onNavigateToFavorites) {
                Text("Favorites")
            }
            Button(onClick = onNavigateToSettings) {
                Text("Settings")
            }
        }
    }
}
