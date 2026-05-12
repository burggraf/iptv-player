package com.iptvplayer.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ListItem
import androidx.tv.material3.Surface
import androidx.tv.material3.Text

data class PlaylistInfo(
    val id: String,
    val name: String,
    val channelCount: Int,
)

/**
 * Playlist selector — TV Material list items for switching between loaded playlists.
 */
@Composable
fun PlaylistSelector(
    modifier: Modifier = Modifier,
    playlists: List<PlaylistInfo> = emptyList(),
    selectedPlaylistId: String? = null,
    onPlaylistSelected: (String) -> Unit = {},
    onAddPlaylist: () -> Unit = {},
) {
    androidx.compose.foundation.layout.Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "Playlists",
            style = TextStyle(fontSize = 14.sp),
            modifier = Modifier.padding(start = 16.dp, bottom = 4.dp),
        )

        if (playlists.isEmpty()) {
            Surface(onClick = onAddPlaylist, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "+ Add Playlist",
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    style = TextStyle(fontSize = 16.sp),
                )
            }
        } else {
            playlists.forEach { info ->
                val selected = info.id == selectedPlaylistId
                Surface(
                    onClick = { onPlaylistSelected(info.id) },
                    selected = selected,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(text = info.name, style = TextStyle(fontSize = 14.sp))
                        Text(
                            text = "(${info.channelCount})",
                            style = TextStyle(fontSize = 12.sp),
                        )
                    }
                }
            }
        }
    }
}
