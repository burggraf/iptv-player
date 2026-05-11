package com.iptvplayer.presentation.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Surface

/**
 * Playlist selector — placeholder.
 * Phase 5 — implement playlist switching UI.
 */
@Composable
fun PlaylistSelector(
    modifier: Modifier = Modifier,
    playlists: List<String> = emptyList(),
    selectedPlaylist: String? = null,
    onPlaylistSelected: (String) -> Unit = {},
) {
    Surface(modifier = modifier.padding(8.dp)) {
        Row {
            if (playlists.isEmpty()) {
                Text("No playlists loaded")
            } else {
                playlists.forEach { name ->
                    Text(
                        text = name,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        }
    }
}
