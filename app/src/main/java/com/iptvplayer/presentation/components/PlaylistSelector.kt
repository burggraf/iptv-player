package com.iptvplayer.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.SelectableSurfaceDefaults
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.iptvplayer.presentation.theme.AppColors

data class PlaylistInfo(
    val id: String,
    val name: String,
    val channelCount: Int,
)

@Composable
fun PlaylistSelector(
    modifier: Modifier = Modifier,
    playlists: List<PlaylistInfo> = emptyList(),
    selectedPlaylistId: String? = null,
    onPlaylistSelected: (String) -> Unit = {},
    onAddPlaylist: () -> Unit = {},
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "Playlists",
            fontSize = 14.sp,
            color = AppColors.TextSecondary,
            modifier = Modifier.padding(start = 16.dp, bottom = 4.dp),
        )

        if (playlists.isEmpty()) {
            Surface(
                onClick = onAddPlaylist,
                shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "+ Add Playlist",
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    fontSize = 16.sp,
                    color = AppColors.TextPrimary,
                )
            }
        } else {
            playlists.forEach { info ->
                val selected = info.id == selectedPlaylistId
                Surface(
                    onClick = { onPlaylistSelected(info.id) },
                    selected = selected,
                    shape = SelectableSurfaceDefaults.shape(RoundedCornerShape(8.dp)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(text = info.name, fontSize = 14.sp, color = AppColors.TextPrimary)
                        Text(
                            text = "(${info.channelCount})",
                            fontSize = 12.sp,
                            color = AppColors.TextTertiary,
                        )
                    }
                }
            }
        }
    }
}
